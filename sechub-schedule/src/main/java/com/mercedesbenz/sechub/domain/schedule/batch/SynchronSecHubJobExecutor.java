// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.batch;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.schedule.ExecutionResult;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.LogConstants;
import com.mercedesbenz.sechub.sharedkernel.messaging.BatchJobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKey;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

class SynchronSecHubJobExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronSecHubJobExecutor.class);
    private DomainMessageService messageService;
    private SecHubJobSafeUpdater secHubJobSafeUpdater;

    public SynchronSecHubJobExecutor(DomainMessageService messageService, SecHubJobSafeUpdater secHubJobSafeUpdater) {
        this.secHubJobSafeUpdater = secHubJobSafeUpdater;
        this.messageService = messageService;
    }

    @IsSendingSyncMessage(MessageID.START_SCAN)
    public void execute(final ScheduleSecHubJob secHubJob, final Long batchJobId) {
        Thread scanThread = new Thread(new Runnable() {

            @Override
            public void run() {
                UUID executionUUID = UUID.randomUUID();

                UUID secHubJobUUID = secHubJob.getUUID();

                String secHubJobUUIDAsString = secHubJobUUID.toString();

                try {
                    String secHubConfiguration = secHubJob.getJsonConfiguration();

                    /* own thread so MDC.put necessary */
                    MDC.clear();
                    MDC.put(LogConstants.MDC_SECHUB_JOB_UUID, secHubJobUUIDAsString);
                    MDC.put(LogConstants.MDC_SECHUB_EXECUTION_UUID, executionUUID);
                    MDC.put(LogConstants.MDC_SECHUB_PROJECT_ID, secHubJob.getProjectId());

                    LOG.info("Executing sechub job: {}", secHubJobUUIDAsString);

                    /* we send now a synchronous SCAN event */
                    DomainMessage request = new DomainMessage(MessageID.START_SCAN);
                    request.set(MessageDataKeys.SECHUB_EXECUTION_UUID, executionUUID);
                    request.set(MessageDataKeys.EXECUTED_BY, secHubJob.getOwner());

                    request.set(MessageDataKeys.SECHUB_JOB_UUID, secHubJobUUID);
                    request.set(MessageDataKeys.SECHUB_CONFIG, MessageDataKeys.SECHUB_CONFIG.getProvider().get(secHubConfiguration));

                    BatchJobMessage batchJobIdMessage = new BatchJobMessage();
                    batchJobIdMessage.setBatchJobId(batchJobId);
                    batchJobIdMessage.setSecHubJobUUID(secHubJobUUID);
                    request.set(MessageDataKeys.BATCH_JOB_ID, batchJobIdMessage);

                    /* wait for scan event result - synchron */
                    DomainMessageSynchronousResult response = messageService.sendSynchron(request);

                    updateSecHubJob(secHubJobUUID, response);

                    sendJobDoneMessage(secHubJobUUID, response);

                } catch (Exception e) {
                    LOG.error("Error happend at spring batch task execution:" + e.getMessage(), e);

                    markSechHubJobFailed(secHubJobUUID);
                    sendJobFailed(secHubJobUUID, TrafficLight.OFF);

                } finally {
                    /* cleanup MDC */
                    MDC.clear();
                }

            }
        }, "scan_" + secHubJob.getUUID());
        scanThread.start();
    }

    private void sendJobDoneMessage(UUID secHubJobUUID, DomainMessageSynchronousResult response) {
        LOG.debug("Will send job done message for: {}", secHubJobUUID);

        String trafficLightAsString = response.get(MessageDataKeys.REPORT_TRAFFIC_LIGHT);

        sendJobDone(secHubJobUUID, TrafficLight.fromString(trafficLightAsString));
    }

    private void markSechHubJobFailed(UUID secHubJobUUID) {
        SecHubMessage jobFailedMessage = new SecHubMessage(SecHubMessageType.ERROR, "The job execution failed.");
        updateSecHubJob(secHubJobUUID, ExecutionResult.FAILED, null, Arrays.asList(jobFailedMessage));

        LOG.info("marked job as failed:{}", secHubJobUUID);
    }

    private void updateSecHubJob(UUID secHubUUID, DomainMessageSynchronousResult response) {
        ExecutionResult result;

        if (response.hasFailed()) {
            result = ExecutionResult.FAILED;
        } else {
            result = ExecutionResult.OK;
        }
        String trafficLightString = response.get(MessageDataKeys.REPORT_TRAFFIC_LIGHT);

        SecHubMessagesList messagesList = response.get(MessageDataKeys.REPORT_MESSAGES);
        List<SecHubMessage> messages = null;
        if (messagesList != null) {
            messages = messagesList.getSecHubMessages();
        }

        updateSecHubJob(secHubUUID, result, trafficLightString, messages);
    }

    private void updateSecHubJob(UUID secHubUUID, ExecutionResult result, String trafficLightString, List<SecHubMessage> messages) {
        secHubJobSafeUpdater.safeUpdateOfSecHubJob(secHubUUID, result, trafficLightString, messages);
    }

    @IsSendingAsyncMessage(MessageID.JOB_DONE)
    private void sendJobDone(UUID jobUUID, TrafficLight trafficLight) {
        sendJobInfoWithTrafficLight(MessageDataKeys.JOB_DONE_DATA, jobUUID, MessageID.JOB_DONE, trafficLight);
    }

    @IsSendingAsyncMessage(MessageID.JOB_FAILED)
    private void sendJobFailed(UUID jobUUID, TrafficLight trafficLight) {
        sendJobInfoWithTrafficLight(MessageDataKeys.JOB_FAILED_DATA, jobUUID, MessageID.JOB_FAILED, trafficLight);
    }

    private void sendJobInfoWithTrafficLight(MessageDataKey<JobMessage> key, UUID jobUUID, MessageID id, TrafficLight trafficLight) {
        DomainMessage request = new DomainMessage(id);
        JobMessage message = createMessage(jobUUID);
        message.setTrafficLight(trafficLight);

        request.set(key, message);

        messageService.sendAsynchron(request);
    }

    private JobMessage createMessage(UUID jobUUID) {
        JobMessage message = new JobMessage();
        message.setJobUUID(jobUUID);
        message.setSince(LocalDateTime.now());
        return message;
    }

}