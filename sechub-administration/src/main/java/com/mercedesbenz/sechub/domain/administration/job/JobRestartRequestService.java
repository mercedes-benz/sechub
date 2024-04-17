// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminRestartsJobHard;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class JobRestartRequestService {

    private static final Logger LOG = LoggerFactory.getLogger(JobRestartRequestService.class);

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    DomainMessageService eventBusService;

    @Autowired
    JobInformationRepository repository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SecHubEnvironment sechubEnvironment;

    @Validated
    @UseCaseAdminRestartsJob(@Step(number = 2, name = "Restart job", description = "Will trigger event that job restart (soft) requested"))
    public void restartJob(UUID jobUUID) {
        assertion.assertIsValidJobUUID(jobUUID);

        auditLogService.log("Requested restart (soft) of job {}", jobUUID);

        JobMessage message = buildMessage(jobUUID);

        /* trigger event */
        triggerJobRestartRequest(message);
    }

    @Validated
    @UseCaseAdminRestartsJobHard(@Step(number = 2, name = "Restart job", description = "Will trigger event that job restart (hard) requested"))
    public void restartJobHard(UUID jobUUID) {
        assertion.assertIsValidJobUUID(jobUUID);

        auditLogService.log("Requested restart (hard) of job {}", jobUUID);

        JobMessage message = buildMessage(jobUUID);

        /* trigger event */
        triggerJobHardRestartRequest(message);
    }

    private JobMessage buildMessage(UUID jobUUID) {
        JobMessage message = new JobMessage();

        message.setJobUUID(jobUUID);

        Optional<JobInformation> optJobInfo = repository.findById(jobUUID);
        if (!optJobInfo.isPresent()) {
            LOG.warn("Did not found job information, so not able to resolve owner email address");
            return message;
        }

        JobInformation jobInfo = optJobInfo.get();
        if (jobInfo.owner == null) {
            LOG.warn("Did not found owner inside job information, so not able to resolve owner email address");
            return message;
        }
        Optional<User> optUser = userRepository.findById(jobInfo.owner);
        if (!optUser.isPresent()) {
            LOG.warn("Did not found owner {} inside user repo, so not able to resolve owner email address", jobInfo.owner);
            return message;
        }
        message.setOwner(jobInfo.owner);
        message.setOwnerEmailAddress(optUser.get().getEmailAddress());
        return message;
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_JOB_RESTART)
    private void triggerJobRestartRequest(JobMessage message) {

        DomainMessage infoRequest = DomainMessageFactory.createEmptyRequest(MessageID.REQUEST_JOB_RESTART);
        infoRequest.set(MessageDataKeys.JOB_RESTART_DATA, message);
        infoRequest.set(MessageDataKeys.ENVIRONMENT_BASE_URL, sechubEnvironment.getServerBaseUrl());

        eventBusService.sendAsynchron(infoRequest);
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_JOB_RESTART_HARD)
    private void triggerJobHardRestartRequest(JobMessage message) {

        DomainMessage infoRequest = DomainMessageFactory.createEmptyRequest(MessageID.REQUEST_JOB_RESTART_HARD);
        infoRequest.set(MessageDataKeys.JOB_RESTART_DATA, message);
        infoRequest.set(MessageDataKeys.ENVIRONMENT_BASE_URL, sechubEnvironment.getServerBaseUrl());

        eventBusService.sendAsynchron(infoRequest);
    }

}
