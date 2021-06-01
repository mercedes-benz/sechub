// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.AdapterRuntimeContext;
import com.daimler.sechub.adapter.pds.data.PDSJobCreateResult;
import com.daimler.sechub.adapter.pds.data.PDSJobData;
import com.daimler.sechub.adapter.pds.data.PDSJobParameterEntry;
import com.daimler.sechub.adapter.pds.data.PDSJobStatus;
import com.daimler.sechub.adapter.pds.data.PDSJobStatus.PDSAdapterJobStatusState;

/**
 * This component is able to handle PDS API V1
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class PDSAdapterV1 extends AbstractAdapter<PDSAdapterContext, PDSAdapterConfig> implements PDSAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(PDSAdapterV1.class);

    @Override
    public int getAdapterVersion() {
        return 1;
    }

    @Override
    protected String execute(PDSAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        assertNotInterrupted();
        PDSContext context = new PDSContext(config, this, runtimeContext);

        createNewPDSJOB(context);
        assertNotInterrupted();

        uploadJobData(context);
        assertNotInterrupted();

        markJobAsReady(context);
        assertNotInterrupted();

        waitForJobDone(context);
        assertNotInterrupted();

        return fetchReport(context);

    }

    private void waitForJobDone(PDSContext context) throws AdapterException {
        PDSAdapterConfig config = context.getConfig();
        UUID jobUUID = config.getSecHubJobUUID();
        UUID uuid = context.getPdsJobUUID();

        int count = 0;
        boolean jobEnded = false;
        PDSJobStatus jobstatus = null;

        long started = getCurrentTimeMilliseconds();

        int timeToWaitForNextCheckOperationInMilliseconds = config.getTimeToWaitForNextCheckOperationInMilliseconds();
        while (!jobEnded && isNotTimeout(config, started)) {
            /* see PDSJobStatusState.java */
            jobstatus = getJobStatus(context);

            PDSAdapterJobStatusState state = jobstatus.state;
            switch (state) {
            case DONE:
            case FAILED:
            case CANCELED:
                jobEnded = true;
                break; // break case...
            default:
                // just do nothing else
            }
            if (jobEnded) {
                break; // break while...
            }
            assertNotInterrupted();
            try {
                Thread.sleep(timeToWaitForNextCheckOperationInMilliseconds);
            } catch (InterruptedException e) {
                throw new AdapterException(getAdapterLogId(null), "Execution thread was interrupted");
            }
            count++;

        }
        if (!jobEnded) {
            long elapsedTimeInMilliseconds = calculateElapsedTime(started);
            throw new IllegalStateException("Even after " + count + " retries, every waiting " + timeToWaitForNextCheckOperationInMilliseconds
                    + " ms, no job report state acceppted as END was found.!\nElapsed time were" + elapsedTimeInMilliseconds
                    + " ms.\nLAST fetched jobstatus for " + jobUUID + ", PDS job uuid: " + uuid + " was:\n" + jobstatus);
        }

    }

    private boolean isNotTimeout(PDSAdapterConfig config, long started) {
        return calculateElapsedTime(started) < config.getTimeOutInMilliseconds();
    }

    private long calculateElapsedTime(long started) {
        return getCurrentTimeMilliseconds() - started;
    }

    private long getCurrentTimeMilliseconds() {
        return System.currentTimeMillis();
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Fetch report.................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private String fetchReport(PDSContext context) throws AdapterException {
        UUID pdsJobUUID = context.getPdsJobUUID();
        String url = context.getUrlBuilder().buildGetJobResult(pdsJobUUID);

        ResponseEntity<String> response = context.getRestOperations().getForEntity(url, String.class);

        return response.getBody();
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Fetch status.................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private PDSJobStatus getJobStatus(PDSContext context) {
        String url = context.getUrlBuilder().buildGetJobStatus(context.getPdsJobUUID());

        ResponseEntity<PDSJobStatus> response = context.getRestOperations().getForEntity(url, PDSJobStatus.class);
        return response.getBody();
    }

    private void markJobAsReady(PDSContext context) {
        UUID uuid = context.getPdsJobUUID();
        String url = context.getUrlBuilder().buildMarkJobReadyToStart(uuid);
        context.getRestSupport().put(url);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Upload.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void uploadJobData(PDSContext context) throws AdapterException {

        PDSAdapterConfig config = context.getConfig();
        /*
         * TODO Albert Tregnaghi, 2021-05-28: hmm.. in future not only
         * PDSSourceZipConfig but more:
         */
        if (!(config instanceof PDSSourceZipConfig)) {
            /* no upload necessary */
            return;
        }

        String useSecHubStorage = config.getJobParameters().get(PDSAdapterConstants.PARAM_KEY_USE_SECHUB_STORAGE);
        if (Boolean.parseBoolean(useSecHubStorage)) {
            LOG.info("Not uploading job data because configuration wants to use SecHub storage");
            return;
        }

        PDSSourceZipConfig sourceZipConfig = (PDSSourceZipConfig) config;
        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();
        if (!metaData.hasValue(PDSAdapterConstants.METADATA_KEY_FILEUPLOAD_DONE, true)) {
            /* upload source code */
            PDSUploadSupport uploadSupport = new PDSUploadSupport();
            uploadSupport.uploadZippedSourceCode(context, sourceZipConfig);

            /* after this - mark file upload done, so on a restart we don't need this */
            metaData.setValue(PDSAdapterConstants.METADATA_KEY_FILEUPLOAD_DONE, true);
            context.getRuntimeContext().getCallback().persist(metaData);
        } else {
            LOG.info("Reuse existing upload for:{}", context.getTraceID());
        }
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Create New Job.................. + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void createNewPDSJOB(PDSContext context) throws AdapterException {

        String json = createJobDataJSON(context);
        String url = context.getUrlBuilder().buildCreateJob();

        String jsonResult = context.getRestSupport().postJSON(url, json);
        PDSJobCreateResult result = context.getJsonSupport().fromJSON(PDSJobCreateResult.class, jsonResult);
        context.setPDSJobUUID(UUID.fromString(result.jobUUID));
    }

    private String createJobDataJSON(PDSContext context) throws AdapterException {
        PDSJobData jobData = createJobData(context);

        String json = context.getJsonSupport().toJSON(jobData);
        return json;
    }

    private PDSJobData createJobData(PDSContext context) {
        PDSAdapterConfig config = context.getConfig();
        Map<String, String> parameters = config.getJobParameters();

        PDSJobData jobData = new PDSJobData();
        for (String key : parameters.keySet()) {
            PDSJobParameterEntry parameter = new PDSJobParameterEntry();
            parameter.key = key;
            parameter.value = parameters.get(key);

            jobData.parameters.add(parameter);
        }

        UUID secHubJobUUID = config.getSecHubJobUUID();
        jobData.sechubJobUUID = secHubJobUUID.toString();
        jobData.productId = config.getPdsProductIdentifier();

        return jobData;
    }

    @Override
    protected String getAPIPrefix() {
        return "/api/";
    }
}
