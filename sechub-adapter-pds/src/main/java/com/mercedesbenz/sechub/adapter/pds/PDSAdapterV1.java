// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import static com.mercedesbenz.sechub.commons.pds.PDSMetaDataKeys.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobCreateResult;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobData;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobParameterEntry;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatus;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;

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

    private static final AdapterExecutionResult NO_EXISTING_ADAPTER_EXECUTION_RESULT = null;

    private PDSUploadSupport uploadSupport;

    @Autowired
    PDSContextFactory contextFactory;

    PDSAdapterV1() {
        uploadSupport = new PDSUploadSupport();
    }

    @Override
    public int getAdapterVersion() {
        return 1;
    }

    @Override
    protected AdapterExecutionResult execute(PDSAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        AdapterExecutionResult result = null;

        assertThreadNotInterrupted();

        PDSContext pdsContext = contextFactory.create(config, this, runtimeContext);
        handleResilienceConfiguration(pdsContext);

        try {
            result = execute(pdsContext);
        } catch (Exception e) {
            if (e instanceof AdapterException) {
                throw (AdapterException) e;
            }
            if (e instanceof RuntimeException) {
                RuntimeException re = (RuntimeException) e;
                throw re;
            }
            throw asAdapterException("Unexpected error: " + e.getMessage(), config);
        }

        return result;

    }

    private void handleResilienceConfiguration(PDSContext pdsContext) {
        PDSAdapterConfig config = pdsContext.getConfig();

        PDSAdapterConfigData data = config.getPDSAdapterConfigData();
        assertConfigDataNotNull(data);

        int maxRetries = data.getResilienceMaxRetries();
        if (maxRetries >= 0) {
            LOG.info("Change resilience max retries to: {}", maxRetries);
            pdsContext.getResilienceConsultant().setMaxRetries(maxRetries);
        }

        long maxWaitInMillis = data.getResilienceTimeToWaitBeforeRetryInMilliseconds();
        if (maxWaitInMillis > 0) {
            LOG.info("Change resilience max wait time to: {} milliseconds", maxRetries);
            pdsContext.getResilienceConsultant().setRetryTimeToWaitInMilliseconds(maxWaitInMillis);
        }
    }

    private AdapterExecutionResult execute(PDSContext pdsContext) throws Exception {

        AdapterExecutionResult alreadyKnownResult = handleExecutionType(pdsContext);
        if (alreadyKnownResult != null) {
            return alreadyKnownResult;
        }

        assertThreadNotInterrupted();
        uploadJobDataIfNecessary(pdsContext);
        assertThreadNotInterrupted();

        markJobAsReadyIfNecessary(pdsContext);
        assertThreadNotInterrupted();

        waitForJobDone(pdsContext);
        assertThreadNotInterrupted();

        return collectAdapterExecutionResult(pdsContext);
    }

    private AdapterExecutionResult collectAdapterExecutionResult(PDSContext context) throws AdapterException {
        return new AdapterExecutionResult(fetchReport(context), fetchMessages(context));
    }

    private void waitForJobDone(PDSContext context) throws Exception {
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

        StateFulTimeOutCheck timeOutCheck = new StateFulTimeOutCheck();

        while (!jobEnded && timeOutCheck.isNotTimeout(config, started)) {

            count++;

            if (LOG.isDebugEnabled()) {
                long currentElapsedTime = calculateElapsedTime(started);

                LOG.debug("Fetch job status for PDS-job:{}. Elapsed time for {} retries:{} ms", pdsJobUUID, count, currentElapsedTime);
            }

            /* see PDSJobStatusState.java */
            jobstatus = context.getResilientJobStatusResultExecutor().executeResilient(() -> getJobStatus(context));

            PDSJobStatusState state = jobstatus.state;
            switch (state) {
            case DONE:
                jobEnded = true;
                break;
            case FAILED:
                throw asAdapterException("PDS job execution failed: TimeOut=" + timeOutCheck.wasTimeOut() + ",JobEnded=" + jobEnded, config);
            case CANCELED:
            case CANCEL_REQUESTED:
                throw asAdapterCanceledByUserException(config);
            default:
                // just do nothing else
            }
            if (jobEnded) {
                break; // break while...
            }

            assertThreadNotInterrupted();

            try {
                Thread.sleep(timeToWaitForNextCheckOperationInMilliseconds);
            } catch (InterruptedException e) {
                throw new AdapterException(getAdapterLogId(null),
                        "Execution thread was interrupted. Type:" + context.getRuntimeContext().getType() + ", Thread was:" + Thread.currentThread().getName());
            }

        }
        if (!jobEnded) {
            long elapsedTimeInMilliseconds = calculateElapsedTime(started);
            throw new IllegalStateException("Even after " + count + " retries, every waiting " + timeToWaitForNextCheckOperationInMilliseconds
                    + " ms, no job report state acceppted as END was found.!\nElapsed time were" + elapsedTimeInMilliseconds
                    + " ms.\nLAST fetched jobstatus for " + secHubJobUUID + ", PDS job uuid: " + pdsJobUUID + " was:\n" + jobstatus);
        }

    }

    private class StateFulTimeOutCheck {
        boolean stillTimeLeft = true;

        boolean isNotTimeout(PDSAdapterConfig config, long started) {
            stillTimeLeft = calculateElapsedTime(started) < config.getTimeOutInMilliseconds();
            return stillTimeLeft;
        }

        boolean wasTimeOut() {
            return !stillTimeLeft;
        }
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

        ResponseEntity<String> response = null;
        response = context.getRestOperations().getForEntity(url, String.class);

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
        UUID pdsJobUUID = context.getPdsJobUUID();
        return getJobStatus(context, pdsJobUUID);
    }

    private PDSJobStatus getJobStatus(PDSContext context, UUID pdsJobUUID) {
        String url = context.getUrlBuilder().buildGetJobStatus(pdsJobUUID);

        ResponseEntity<PDSJobStatus> response = context.getRestOperations().getForEntity(url, PDSJobStatus.class);
        return response.getBody();
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Mark JOB as ready............... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void markJobAsReadyIfNecessary(PDSContext context) throws Exception {

        AdapterRuntimeContext runtimeContext = context.getRuntimeContext();
        AdapterMetaData metaData = runtimeContext.getMetaData();
        if (metaData.getValueAsBoolean(PDS_JOB_MARKED_AS_READY)) {
            LOG.info("Mark job ready skipped for pds job: {}, becausse already marked as ready");
            /* already uploaded */
            return;
        }

        UUID uuid = context.getPdsJobUUID();
        String url = context.getUrlBuilder().buildMarkJobReadyToStart(uuid);
        context.getResilientRunOrFailExecutor().executeResilient(() -> context.getRestSupport().put(url));

        metaData.setValue(PDS_JOB_MARKED_AS_READY, true);

        runtimeContext.getCallback().persist(metaData);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Upload.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void uploadJobDataIfNecessary(PDSContext context) throws Exception {
        PDSAdapterConfig config = context.getConfig();
        PDSAdapterConfigData data = config.getPDSAdapterConfigData();

        if (data.isReusingSecHubStorage()) {
            LOG.info("No upload necessary: PDS job {} reuses SecHub storage for {}", context.getPdsJobUUID(), context.getTraceID());
            return;
        }

        /* PDS has other storage - we must upload content */
        AdapterRuntimeContext runtimeContext = context.getRuntimeContext();
        AdapterMetaData metaData = runtimeContext.getMetaData();
        if (metaData.getValueAsBoolean(PDS_JOB_UPLOAD_DONE)) {
            LOG.info("Upload skipped for pds job: {}, becausse already uploaded");
            /* already uploaded */
            return;
        }
        handleUploadWhenRequired(context, SecHubDataConfigurationType.SOURCE);
        handleUploadWhenRequired(context, SecHubDataConfigurationType.BINARY);

        metaData.setValue(PDS_JOB_UPLOAD_DONE, true);

        runtimeContext.getCallback().persist(metaData);
    }

    private void handleUploadWhenRequired(PDSContext context, SecHubDataConfigurationType type) throws Exception {
        PDSAdapterConfig config = context.getConfig();
        PDSAdapterConfigData data = config.getPDSAdapterConfigData();

        UUID pdsJobUUID = context.getPdsJobUUID();
        String secHubTraceId = context.getTraceID();
        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();

        boolean required = checkUploadRequired(data, type);

        if (!required) {
            LOG.debug("Skipped {} file upload for pds job:{}, because not required", type, pdsJobUUID);
            return;
        }

        String sourceUploadMetaDataKey = createUploadMetaDataKey(pdsJobUUID, type);

        if (metaData.getValueAsBoolean(sourceUploadMetaDataKey)) {
            LOG.info("Reuse existing {} upload for pds job: {} - sechub: {}", type, pdsJobUUID, secHubTraceId);
            return;
        }

        LOG.info("Start {} uploading for pds job: {} - sechub: {}", type, pdsJobUUID, secHubTraceId);

        String checksum = fetchChecksumOrNull(data, type);

        Long fileSize = fetchFileSizeOrNull(data, type);
        String fileSizeAsString = null;
        if (fileSize != null) {
            fileSizeAsString = "" + fileSize;
        }
        final String finalFileSizeAsString = fileSizeAsString;

        context.getResilientRunOrFailExecutor().executeResilient(() -> uploadSupport.upload(type, context, data, checksum, finalFileSizeAsString));

        /* after this - mark file upload done - at least for debugging */
        metaData.setValue(sourceUploadMetaDataKey, true);
        context.getRuntimeContext().getCallback().persist(metaData);
    }

    private boolean checkUploadRequired(PDSAdapterConfigData data, SecHubDataConfigurationType type) {
        switch (type) {
        case NONE:
            return false;
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
        case NONE:
            return null;
        case BINARY:
            return data.getBinariesTarFileChecksumOrNull();
        case SOURCE:
            return data.getSourceCodeZipFileChecksumOrNull();
        default:
            throw new IllegalArgumentException("scan type: " + type + " is not supported!");
        }
    }

    private Long fetchFileSizeOrNull(PDSAdapterConfigData data, SecHubDataConfigurationType type) {
        switch (type) {
        case NONE:
            return null;
        case BINARY:
            return data.getBinariesTarFileSizeInBytesOrNull();
        case SOURCE:
            return data.getSourceCodeZipFileSizeInBytesOrNull();
        default:
            throw new IllegalArgumentException("scan type: " + type + " is not supported!");
        }
    }

    private String createUploadMetaDataKey(UUID pdsJobUUID, SecHubDataConfigurationType type) {
        switch (type) {
        case NONE:
            return null;
        case BINARY:
            return PDSMetaDataID.createBinaryUploadDoneKey(pdsJobUUID);
        case SOURCE:
            return PDSMetaDataID.createSourceUploadDoneKey(pdsJobUUID);
        default:
            throw new IllegalArgumentException("scan type: " + type + " is not supported!");
        }
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Execution type handling ........ + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private AdapterExecutionResult handleExecutionType(PDSContext context) throws Exception {
        AdapterRuntimeContext runtimeContext = context.getRuntimeContext();

        ExecutionType executionType = runtimeContext.getType();

        switch (executionType) {
        case INITIAL:
            return handleExecutionTypeInitial(context, runtimeContext);
        case RESTART:
            return handleExecutionTypeRestart(context, runtimeContext);
        case CANCEL:
            return handleExecutionTypeCancel(context, runtimeContext);
        default:
            throw new IllegalStateException("the execution type: " + executionType + " is not supported");
        }
    }

    private AdapterExecutionResult handleExecutionTypeInitial(PDSContext context, AdapterRuntimeContext runtimeContext) throws Exception {
        LOG.debug("Initial pds job creation necessary");

        String pdsJobUUID = createNewPDSJobAndRememberInAdapterMetaData(context, runtimeContext);
        context.setPDSJobUUID(UUID.fromString(pdsJobUUID));

        return null;
    }

    private AdapterExecutionResult handleExecutionTypeRestart(PDSContext context, AdapterRuntimeContext runtimeContext) throws Exception {
        AdapterMetaData metaData = runtimeContext.getMetaData();
        String pdsJobUUID = metaData.getValueAsStringOrNull(PDS_JOB_UUID);

        if (pdsJobUUID != null && !pdsJobUUID.isEmpty()) {
            LOG.debug("Restart in progress, try to reuse PDS job: {}", pdsJobUUID);

            PDSJobStatusState currentPdsJobState = null;

            /* check job status */
            try {
                PDSJobStatus currentPdsJobStatus = getJobStatus(context, UUID.fromString(pdsJobUUID));
                currentPdsJobState = currentPdsJobStatus.state;

                if (currentPdsJobState == null) {
                    throw new IllegalStateException("PDS job state null is not supported!");
                }

                switch (currentPdsJobState) {

                case DONE:
                    /* just fetch the former PDS result */
                    LOG.info("Reuse existing result from PDS job: {} because job is in state: {}.", pdsJobUUID, currentPdsJobState);

                    context.setPDSJobUUID(UUID.fromString(pdsJobUUID)); // update context with new UUID
                    return collectAdapterExecutionResult(context);

                case CANCELED:
                case CANCEL_REQUESTED:
                case FAILED:
                    LOG.info("Cannot reuse PDS job: {} because in state {} where result cannot be reused.", pdsJobUUID, currentPdsJobState);
                    pdsJobUUID = null;
                    break;
                case CREATED:
                case QUEUED:
                case READY_TO_START:
                case RUNNING:
                    LOG.info("PDS job: {} found in state: {}. Means not finished. So just reuse existing job.", pdsJobUUID, currentPdsJobState);
                    break;
                default:
                    throw new IllegalStateException("Job state: " + currentPdsJobState + " is not supported!");

                }
            } catch (RuntimeException e) {
                LOG.error("Was not able to reuse former PDS job: {}, old job state was: {}.", pdsJobUUID, currentPdsJobState, e);

                pdsJobUUID = null;// reset
            }

        } else {
            LOG.warn("Cannot restart PDS job because no pds Job UUID was set");
        }

        if (pdsJobUUID == null || pdsJobUUID.isEmpty()) {
            LOG.warn("Will create new PDS job as fallback for restart.", pdsJobUUID);
            pdsJobUUID = createNewPDSJobAndRememberInAdapterMetaData(context, runtimeContext);
        }
        context.setPDSJobUUID(UUID.fromString(pdsJobUUID));

        return NO_EXISTING_ADAPTER_EXECUTION_RESULT;
    }

    private AdapterExecutionResult handleExecutionTypeCancel(PDSContext context, AdapterRuntimeContext runtimeContext) throws Exception {
        AdapterMetaData metaData = runtimeContext.getMetaData();
        String pdsJobUUID = metaData.getValueAsStringOrNull(PDS_JOB_UUID);

        if (pdsJobUUID == null || pdsJobUUID.isEmpty()) {
            LOG.error("PDS job uuid from adapter meta data was :{}, so stop not possible.", pdsJobUUID);
            throw asAdapterException("PDS job uuid not set, cannot cancel", context);
        }
        context.setPDSJobUUID(UUID.fromString(pdsJobUUID));
        cancelJob(context);

        return AdapterExecutionResult.createCancelResult();
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................New Job......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private String createNewPDSJobAndRememberInAdapterMetaData(PDSContext context, AdapterRuntimeContext runtimeContext) throws Exception {
        String json = createJobDataJSON(context);
        String url = context.getUrlBuilder().buildCreateJob();

        String jsonResult = context.getResilientStringResultExecutor().executeResilient(() -> context.getRestSupport().postJSON(url, json));
        PDSJobCreateResult result = context.getJsonSupport().fromJSON(PDSJobCreateResult.class, jsonResult);
        String pdsJobUUID = result.jobUUID;

        LOG.info("New PDS job created with PDS job uuid:{}", pdsJobUUID);

        AdapterMetaData metaData = runtimeContext.getMetaData();
        metaData.setValue(PDS_JOB_UUID, pdsJobUUID);
        metaData.setValue(PDS_JOB_MARKED_AS_READY, false);
        metaData.setValue(PDS_JOB_UPLOAD_DONE, false);

        runtimeContext.getCallback().persist(metaData);
        return pdsJobUUID;
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Cancel Job ....................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void cancelJob(PDSContext context) throws Exception {
        UUID pdsJobUUID = context.getPdsJobUUID();

        String url = context.getUrlBuilder().buildCancelJob(pdsJobUUID);
        context.getResilientRunOrFailExecutor().executeResilient(() -> context.getRestSupport().put(url));

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
        assertConfigDataNotNull(data);
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

    private void assertConfigDataNotNull(PDSAdapterConfigData data) {
        if (data == null) {
            throw new IllegalStateException("Adapter config data may not be null!");
        }
    }

    @Override
    protected String getAPIPrefix() {
        return "/api/";
    }
}
