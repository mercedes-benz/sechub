// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.batch.SynchronSecHubJobExecutor;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseSchedulerStartsJob;

/**
 * This service is only responsible to trigger job execution for given
 * {@link ScheduleSecHubJob} by executor and to send the initial JOB_STARTED
 * event. The executor will send additional events and does the job execution
 * itself.
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScheduleJobLauncherService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleJobLauncherService.class);

    @Autowired
    @Lazy
    DomainMessageService eventBus;

    @Autowired
    SynchronSecHubJobExecutor executor;

    @UseCaseSchedulerStartsJob(@Step(number = 2, next = { 3,
            4 }, name = "Execution", description = "Triggers job execution - done parallel, but with synchronous domain communication (to wait for result)."))
    public void executeJob(ScheduleSecHubJob secHubJob) {
        UUID secHubJobUUID = secHubJob.getUUID();

        LOG.debug("Execute job:{}", secHubJobUUID);

        executor.execute(secHubJob);

        /* send domain event */
        sendJobStarted(secHubJob.getProjectId(), secHubJobUUID, secHubJob.getJsonConfiguration(), secHubJob.getOwner());

    }

    @IsSendingAsyncMessage(MessageID.JOB_STARTED)
    private void sendJobStarted(String projectId, UUID jobUUID, String configuration, String owner) {
        DomainMessage request = new DomainMessage(MessageID.JOB_STARTED);
        JobMessage message = new JobMessage();
        message.setProjectId(projectId);
        message.setJobUUID(jobUUID);
        message.setConfiguration(configuration);
        message.setOwner(owner);
        message.setSince(LocalDateTime.now());

        request.set(MessageDataKeys.JOB_STARTED_DATA, message);

        eventBus.sendAsynchron(request);
    }

}
