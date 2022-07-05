// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AbstractAdapter;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterProfiles;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext.ExecutionType;
import com.mercedesbenz.sechub.adapter.pds.data.PDSJobCreateResult;
import com.mercedesbenz.sechub.adapter.pds.data.PDSJobData;
import com.mercedesbenz.sechub.adapter.pds.data.PDSJobParameterEntry;
import com.mercedesbenz.sechub.adapter.pds.data.PDSJobStatus;
import com.mercedesbenz.sechub.adapter.pds.data.PDSJobStatus.PDSAdapterJobStatusState;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;

/**
 * This component is able to handle PDS API V1
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class PDSAdapterV1 extends AbstractAdapter<PDSAdapterContext, PDSAdapterConfig> implements PDSAdapter {

    private static final String PDS_JOB_UUID = "PDS_JOB_UUID";

    private static final Logger LOG = LoggerFactory.getLogger(PDSAdapterV1.class);

    private PDSUploadSupport uploadSupport;

    PDSAdapterV1() {
        uploadSupport = new PDSUploadSupport();
    }

    @Override
    public int getAdapterVersion() {
        return 1;
    }

    @Override
    protected AdapterExecutionResult execute(PDSAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        assertNotInterrupted();

        PDSContext context = new PDSContext(config, this, runtimeContext);
        initForExecutionType(context, runtimeContext);

        if (handledStopRequest(context, runtimeContext)) {
            AdapterExecutionResult result = new AdapterExecutionResult("");
            return result;
        }

        assertNotInterrupted();

        uploadJobData(context);
        assertNotInterrupted();

        markJobAsReady(context);
        assertNotInterrupted();

        waitForJobDone(context);
        assertNotInterrupted();

        return new AdapterExecutionResult(fetchReport(context), fetchMessages(context));

    }

    private boolean handledStopRequest(PDSContext context, AdapterRuntimeContext runtimeContext) {
        if (runtimeContext.getType() == ExecutionType.STOP) {
            cancelJob(context);
            return true;
        }
        return false;
    }

    private void waitForJobDone(PDSContext context) throws AdapterException {
        PDSAdapterConfig config = context.getConfig();
        PDSAdapterConfigData data = config.getPDSAdapterConfigData();

        UUID secHubJobUUID = data.getSecHubJobUUID();
        UUID pdsJobUUID = context.getPdsJobUUID();

        int count = 0;
        boolean jobEnded = false;
        PDSJobStatus jobstatus = null;

        long started = getCurrentTimeMilliseconds();

        int timeToWaitForNextCheckOperationInMilliseconds = config.getTimeToWaitForNextCheckOperationInMilliseconds();

        /* @formatter:off */
        LOG.info("Start waiting for PDS-job:{} to be done. "
                + "Related SecHub-Job is: {}. "
                + "Will check every {} ms. "
                + "Adapter will wait maximum {} ms before timeout.",
                pdsJobUUID, 
                secHubJobUUID, 
                timeToWaitForNextCheckOperationInMilliseconds, 
                config.getTimeOutInMilliseconds());
        /* @formatter:on */

        while (!jobEnded && isNotTimeout(config, started)) {

            count++;

            if (LOG.isDebugEnabled()) {
                long currentElapsedTime = calculateElapsedTime(started);
                
                LOG.debug("Fetch job status for PDS-job:{}. Elapsed time for {} retries:{} ms", pdsJobUUID, count, currentElapsedTime);
            }

            /* see PDSJobStatusState.java */
            jobstatus = getJobStatus(context);

            PDSAdapterJobStatusState state = jobstatus.state;
            switch (state) {
            case DONE:
                jobEnded = true;
                break;
            case FAILED:
                throw asAdapterException("PDS job execution failed", config);
            case CANCELED:
                throw asAdapterCanceledByUserException(config);
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

        }
        if (!jobEnded) {
            long elapsedTimeInMilliseconds = calculateElapsedTime(started);
            throw new IllegalStateException("Even after " + count + " retries, every waiting " + timeToWaitForNextCheckOperationInMilliseconds
                    + " ms, no job report state acceppted as END was found.!\nElapsed time were" + elapsedTimeInMilliseconds
                    + " ms.\nLAST fetched jobstatus for " + secHubJobUUID + ", PDS job uuid: " + pdsJobUUID + " was:\n" + jobstatus);
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
    /* + ................Fetch messages.................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private Collection<SecHubMessage> fetchMessages(PDSContext context) throws AdapterException {
        UUID pdsJobUUID = context.getPdsJobUUID();
        String url = context.getUrlBuilder().buildGetJobMessages(pdsJobUUID);

        ResponseEntity<String> response = context.getRestOperations().getForEntity(url, String.class);

        String body = response.getBody();
        if (body == null || body.isEmpty()) {
            return Collections.emptyList();
        }
        SecHubMessagesList messagesList = SecHubMessagesList.fromJSONString(body);
        return messagesList.getSecHubMessages();
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
        PDSAdapterConfigData data = config.getPDSAdapterConfigData();

        if (data.isReusingSecHubStorage()) {
            LOG.info("No upload necessary: PDS job {} reuses SecHub storage for {}", context.getPdsJobUUID(), context.getTraceID());
            return;
        }

        /* PDS has other storage - we must upload content */
        handleUploadWhenRequired(context, SecHubDataConfigurationType.SOURCE);
        handleUploadWhenRequired(context, SecHubDataConfigurationType.BINARY);
    }

    private void handleUploadWhenRequired(PDSContext context, SecHubDataConfigurationType type) throws AdapterException {
        PDSAdapterConfig config = context.getConfig();
        PDSAdapterConfigData data = config.getPDSAdapterConfigData();

        UUID pdsJobUUID = context.getPdsJobUUID();
        String secHubTraceId = context.getTraceID();
        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();

        boolean required = checkRequired(data, type);

        if (!required) {
            LOG.debug("Skipped {} file upload for pds job:{}, because not required", type, pdsJobUUID);
            return;
        }

        String sourceUploadMetaDataKey = createUploadMetaDataKey(pdsJobUUID, type);

        if (metaData.hasValue(sourceUploadMetaDataKey, true)) {
            LOG.info("Reuse existing {} upload for pds job: {} - sechub: {}", type, pdsJobUUID, secHubTraceId);
            return;
        }

        LOG.info("Start {} uploading for pds job: {} - sechub: {}", type, pdsJobUUID, secHubTraceId);

        String checksum = fetchChecksumOrNull(data, type);
        uploadSupport.upload(type, context, data, checksum);

        /* after this - mark file upload done - at least for debugging */
        metaData.setValue(sourceUploadMetaDataKey, true);
        context.getRuntimeContext().getCallback().persist(metaData);
    }

    private boolean checkRequired(PDSAdapterConfigData data, SecHubDataConfigurationType type) {
        switch (type) {
        case BINARY:
            return data.isBinaryTarFileRequired();
        case SOURCE:
            return data.isSourceCodeZipFileRequired();
        default:
            throw new IllegalArgumentException("scan type: " + type + " is not supported!");
        }
    }

    private String fetchChecksumOrNull(PDSAdapterConfigData data, SecHubDataConfigurationType type) {
        switch (type) {
        case BINARY:
            return data.getBinariesTarFileChecksumOrNull();
        case SOURCE:
            return data.getSourceCodeZipFileChecksumOrNull();
        default:
            throw new IllegalArgumentException("scan type: " + type + " is not supported!");
        }
    }

    private String createUploadMetaDataKey(UUID pdsJobUUID, SecHubDataConfigurationType type) {
        switch (type) {
        case BINARY:
            return PDSMetaDataID.createBinaryUploadDoneKey(pdsJobUUID);
        case SOURCE:
            return PDSMetaDataID.createSourceUploadDoneKey(pdsJobUUID);
        default:
            throw new IllegalArgumentException("scan type: " + type + " is not supported!");
        }
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Initialize for execution type... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void initForExecutionType(PDSContext context, AdapterRuntimeContext runtimeContext) throws AdapterException {
        String pdsJobUUID = null;
        ExecutionType type = runtimeContext.getType();
        switch (type) {
        case INITIAL:
            pdsJobUUID = createNewPDSJob(context, runtimeContext);
            break;
        case RESTART:
            pdsJobUUID = runtimeContext.getMetaData().getValue(PDS_JOB_UUID);
            if (pdsJobUUID == null || pdsJobUUID.isEmpty()) {
                LOG.warn("PDS job uuid was :{}, so restart not possible. Will create new PDS job as fallback.", pdsJobUUID);
                pdsJobUUID = createNewPDSJob(context, runtimeContext);
            } else {
                LOG.info("Restart in progress, will reuse PDS job: {}", pdsJobUUID);
            }
            break;
        case STOP:
            pdsJobUUID = runtimeContext.getMetaData().getValue(PDS_JOB_UUID);
            if (pdsJobUUID == null || pdsJobUUID.isEmpty()) {
                LOG.error("PDS job uuid was :{}, so stop not possible.", pdsJobUUID);
                throw asAdapterException("PDS job uuid not set, cannot cancel", context);
            }
            break;
        default:
            throw new IllegalStateException("the type: " + type + " is not supported");

        }
        context.setPDSJobUUID(UUID.fromString(pdsJobUUID));

    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................New Job......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private String createNewPDSJob(PDSContext context, AdapterRuntimeContext runtimeContext) throws AdapterException {
        String json = createJobDataJSON(context);
        String url = context.getUrlBuilder().buildCreateJob();

        String jsonResult = context.getRestSupport().postJSON(url, json);
        PDSJobCreateResult result = context.getJsonSupport().fromJSON(PDSJobCreateResult.class, jsonResult);
        String pdsJobUUID = result.jobUUID;

        LOG.info("New PDS job created with PDS job uuid:{}", pdsJobUUID);

        AdapterMetaData metaData = runtimeContext.getMetaData();
        metaData.setValue(PDS_JOB_UUID, pdsJobUUID);

        runtimeContext.getCallback().persist(metaData);
        return pdsJobUUID;
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Cancel Job ....................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void cancelJob(PDSContext context) {
        UUID pdsJobUUID = context.getPdsJobUUID();

        String url = context.getUrlBuilder().buildCancelJob(pdsJobUUID);
        context.getRestSupport().put(url);

        LOG.info("PDS job canceled: {}", pdsJobUUID);

    }

    private String createJobDataJSON(PDSContext context) throws AdapterException {
        PDSJobData jobData = createJobData(context);

        String json = context.getJsonSupport().toJSON(jobData);
        return json;
    }

    private PDSJobData createJobData(PDSContext context) {
        PDSAdapterConfig config = context.getConfig();
        PDSAdapterConfigData data = config.getPDSAdapterConfigData();

        Map<String, String> parameters = data.getJobParameters();

        PDSJobData jobData = new PDSJobData();
        for (String key : parameters.keySet()) {
            PDSJobParameterEntry parameter = new PDSJobParameterEntry();
            parameter.key = key;
            parameter.value = parameters.get(key);

            jobData.parameters.add(parameter);
        }

        UUID secHubJobUUID = data.getSecHubJobUUID();
        jobData.sechubJobUUID = secHubJobUUID.toString();
        jobData.productId = data.getPdsProductIdentifier();

        return jobData;
    }

    @Override
    protected String getAPIPrefix() {
        return "/api/";
    }
}
