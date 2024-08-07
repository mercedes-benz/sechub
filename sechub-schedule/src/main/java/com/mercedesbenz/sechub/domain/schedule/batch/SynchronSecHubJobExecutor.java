// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.batch;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.domain.schedule.UUIDContainer;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubConfigurationModelAccessService;
import com.mercedesbenz.sechub.sharedkernel.LogConstants;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKey;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

/**
 * This component executes SecHub jobs in own worker threads (means
 * parallel/asynchronous) but the event handling inside the worker thread is
 * done synchronous to wait inside scheduler domain for scan results from other
 * domain (scan) - this is the reason for the naming.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class SynchronSecHubJobExecutor {

    private static final String SECHUB_SCHEDULE_THREAD_PREFIX = "sechub-schedule:";

    private static final Logger LOG = LoggerFactory.getLogger(SynchronSecHubJobExecutor.class);

    @Autowired
    @Lazy
    DomainMessageService messageService;

    @Autowired
    SecHubJobSafeUpdater secHubJobSafeUpdater;

    @Autowired
    SecHubConfigurationModelAccessService configurationModelAccess;

    @IsSendingSyncMessage(MessageID.START_SCAN)
    public void execute(final ScheduleSecHubJob secHubJob) {
        Thread scheduleWorkerThread = new Thread(() -> executeInsideThread(secHubJob), SECHUB_SCHEDULE_THREAD_PREFIX + secHubJob.getUUID());
        scheduleWorkerThread.start();
    }

    private void executeInsideThread(final ScheduleSecHubJob secHubJob) {
        UUIDContainer uuids = new UUIDContainer();
        uuids.setExecutionUUID(UUID.randomUUID());
        uuids.setSecHubJobUUID(secHubJob.getUUID());

        try {
            String secHubConfiguration = configurationModelAccess.resolveUnencryptedConfigurationasJson(secHubJob);

            /* own thread so MDC.put necessary */
            MDC.clear();
            MDC.put(LogConstants.MDC_SECHUB_JOB_UUID, uuids.getSecHubJobUUIDasString());
            MDC.put(LogConstants.MDC_SECHUB_EXECUTION_UUID, uuids.getExecutionUUIDAsString());
            MDC.put(LogConstants.MDC_SECHUB_PROJECT_ID, secHubJob.getProjectId());

            LOG.info("Executing sechub job: {}, execution uuid: {}", uuids.getSecHubJobUUIDasString(), uuids.getExecutionUUIDAsString());

            sendJobExecutionStartingEvent(secHubJob, uuids);

            /* we send now a synchronous SCAN event */
            DomainMessage startScanRequest = new DomainMessage(MessageID.START_SCAN);
            startScanRequest.set(MessageDataKeys.SECHUB_EXECUTION_UUID, uuids.getExecutionUUID());
            startScanRequest.set(MessageDataKeys.EXECUTED_BY, secHubJob.getOwner());

            startScanRequest.set(MessageDataKeys.SECHUB_JOB_UUID, uuids.getSecHubJobUUID());
            startScanRequest.set(MessageDataKeys.SECHUB_UNENCRYPTED_CONFIG, MessageDataKeys.SECHUB_UNENCRYPTED_CONFIG.getProvider().get(secHubConfiguration));

            /* wait for scan event result - synchron */
            DomainMessageSynchronousResult response = messageService.sendSynchron(startScanRequest);

            updateSecHubJob(uuids, response);

            sendJobDoneMessage(uuids, response);

        } catch (Exception e) {
            LOG.error("Error happend at spring batch task execution:" + e.getMessage(), e);

            markSechHubJobFailed(uuids);
            sendJobFailed(uuids, TrafficLight.OFF);

        } finally {
            /* cleanup MDC */
            MDC.clear();
        }
    }

    @IsSendingAsyncMessage(MessageID.JOB_EXECUTION_STARTING)
    private void sendJobExecutionStartingEvent(final ScheduleSecHubJob secHubJob, UUIDContainer uuids) {
        /* we send asynchronous an information event */
        DomainMessage jobExecRequest = new DomainMessage(MessageID.JOB_EXECUTION_STARTING);

        jobExecRequest.set(MessageDataKeys.SECHUB_EXECUTION_UUID, uuids.getExecutionUUID());
        jobExecRequest.set(MessageDataKeys.SECHUB_JOB_UUID, uuids.getSecHubJobUUID());
        jobExecRequest.set(MessageDataKeys.LOCAL_DATE_TIME_SINCE, secHubJob.getStarted());

        messageService.sendAsynchron(jobExecRequest);
    }

    private void sendJobDoneMessage(UUIDContainer uuids, DomainMessageSynchronousResult response) {
        LOG.debug("Will send job done message for: {}", uuids.getSecHubJobUUIDasString());

        String trafficLightAsString = response.get(MessageDataKeys.REPORT_TRAFFIC_LIGHT);

        sendJobDone(uuids, TrafficLight.fromString(trafficLightAsString));
    }

    private void markSechHubJobFailed(UUIDContainer uuids) {
        SecHubMessage jobFailedMessage = new SecHubMessage(SecHubMessageType.ERROR, "The job execution failed.");
        updateSecHubJob(uuids, ExecutionResult.FAILED, null, Arrays.asList(jobFailedMessage));

        LOG.info("marked job as failed - sechub job: {}, execution-uuid: {}", uuids.getSecHubJobUUIDasString(), uuids.getExecutionUUIDAsString());
    }

    private void updateSecHubJob(UUIDContainer uuids, DomainMessageSynchronousResult response) {
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

        updateSecHubJob(uuids, result, trafficLightString, messages);
    }

    private void updateSecHubJob(UUIDContainer uuids, ExecutionResult result, String trafficLightString, List<SecHubMessage> messages) {
        secHubJobSafeUpdater.safeUpdateOfSecHubJob(uuids.getSecHubJobUUID(), result, trafficLightString, messages);
    }

    @IsSendingAsyncMessage(MessageID.JOB_DONE)
    private void sendJobDone(UUIDContainer uuids, TrafficLight trafficLight) {
        sendJobInfoWithTrafficLight(MessageDataKeys.JOB_DONE_DATA, uuids, MessageID.JOB_DONE, trafficLight);
    }

    @IsSendingAsyncMessage(MessageID.JOB_FAILED)
    private void sendJobFailed(UUIDContainer uuids, TrafficLight trafficLight) {
        sendJobInfoWithTrafficLight(MessageDataKeys.JOB_FAILED_DATA, uuids, MessageID.JOB_FAILED, trafficLight);
    }

    private void sendJobInfoWithTrafficLight(MessageDataKey<JobMessage> key, UUIDContainer uuids, MessageID id, TrafficLight trafficLight) {
        DomainMessage request = new DomainMessage(id);
        JobMessage message = createMessage(uuids);
        message.setTrafficLight(trafficLight);
        request.set(key, message);
        request.set(MessageDataKeys.SECHUB_EXECUTION_UUID, uuids.getExecutionUUID());

        messageService.sendAsynchron(request);
    }

    private JobMessage createMessage(UUIDContainer uuids) {
        JobMessage message = new JobMessage();
        message.setJobUUID(uuids.getSecHubJobUUID());
        message.setSince(LocalDateTime.now());
        return message;
    }

}