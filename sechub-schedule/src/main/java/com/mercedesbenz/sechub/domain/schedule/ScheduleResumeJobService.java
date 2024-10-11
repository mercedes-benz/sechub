// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.other.UseCaseSystemResumesSuspendedJobs;

@Service
public class ScheduleResumeJobService {

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    SecHubEnvironment sechubEnvironment;

    @UseCaseSystemResumesSuspendedJobs(@Step(number = 4, name = "Resume job", description = """
            The SecHub job will be resumed. This is done by triggering a soft restart request for the
            job.

            You can read <<section-usecase-UC_041,Admin restarts a job>> for the steps which are
            done at restart process. The steps are the same, except there is no audit logging
            and the event is not triggered from `administration` but `schedule` domain.
            """))
    public void resume(ScheduleSecHubJob next) {
        triggerJobRestartRequest(next.getUUID());
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_JOB_RESTART)
    private void triggerJobRestartRequest(UUID jobUUID) {

        JobMessage message = new JobMessage();
        message.setJobUUID(jobUUID);

        // we do NOT use REQUEST_JOB_RESTART_HARD ! Reason: hard would destroy all
        // former data - but we want to reuse the exiting results...
        DomainMessage restartJobRequest = DomainMessageFactory.createEmptyRequest(MessageID.REQUEST_JOB_RESTART);
        restartJobRequest.set(MessageDataKeys.JOB_RESTART_DATA, message);
        restartJobRequest.set(MessageDataKeys.ENVIRONMENT_BASE_URL, sechubEnvironment.getServerBaseUrl());

        eventBusService.sendAsynchron(restartJobRequest);
    }

}
