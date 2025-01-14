// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotAuthorizedException;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminCancelsJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseUserCancelsJob;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class JobCancelService {

    private static final Logger LOG = LoggerFactory.getLogger(JobCancelService.class);
    private final AuditLogService auditLogService;
    private final UserInputAssertion userInputAssertion;
    private final DomainMessageService eventBusService;
    private final JobInformationRepository jobInformationRepository;
    private final UserRepository userRepository;

    public JobCancelService(AuditLogService auditLogService, UserInputAssertion userInputAssertion, DomainMessageService eventBusService,
            JobInformationRepository jobInformationRepository, UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.userInputAssertion = userInputAssertion;
        this.eventBusService = eventBusService;
        this.jobInformationRepository = jobInformationRepository;
        this.userRepository = userRepository;
    }

    @Validated
    @UseCaseAdminCancelsJob(@Step(number = 2, name = "Cancel job", description = "Will trigger event that job cancel requested"))
    public void cancelJob(UUID jobUUID) {
        userInputAssertion.assertIsValidJobUUID(jobUUID);

        auditLogService.log("Requested cancellation of job {}", jobUUID);

        JobMessage message = buildMessage(jobUUID);

        /* trigger event */
        informCancelJobRequested(message);
    }

    @Validated
    @UseCaseUserCancelsJob(@Step(number = 2, name = "Cancel job", description = "Will trigger event that job cancel requested"))
    public void userCancelJob(UUID jobUUID, String userId) {
        userInputAssertion.assertIsValidJobUUID(jobUUID);
        userInputAssertion.assertIsValidUserId(userId);

        auditLogService.log("User {} requested cancellation of job {}", userId, jobUUID);

        assertUserAllowedCancelJob(jobUUID, userId);
        JobMessage message = buildMessage(jobUUID);

        /* trigger event */
        informCancelJobRequested(message);
    }

    private void assertUserAllowedCancelJob(UUID jobUUID, String userId) {
        JobInformation jobInfo = jobInformationRepository.findById(jobUUID).orElseThrow(() -> new NotFoundException("Job not found: " + jobUUID));

        User user = userRepository.findOrFailUser(userId);
        for (Project project : user.getProjects()) {
            if (project.getId().equals(jobInfo.getProjectId())) {
                return;
            }
        }
        throw new NotAuthorizedException("User not allowed to cancel job: " + jobUUID);
    }

    private JobMessage buildMessage(UUID jobUUID) {
        JobMessage message = new JobMessage();

        message.setJobUUID(jobUUID);

        Optional<JobInformation> optJobInfo = jobInformationRepository.findById(jobUUID);
        if (optJobInfo.isEmpty()) {
            LOG.warn("Did not found job information, so not able to resolve owner email address");
            return message;
        }

        JobInformation jobInfo = optJobInfo.get();
        if (jobInfo.owner == null) {
            LOG.warn("Did not found owner inside job information, so not able to resolve owner email address");
            return message;
        }
        Optional<User> optUser = userRepository.findById(jobInfo.owner);
        if (optUser.isEmpty()) {
            LOG.warn("Did not found owner {} inside user repo, so not able to resolve owner email address", jobInfo.owner);
            return message;
        }
        message.setOwner(jobInfo.owner);
        message.setOwnerEmailAddress(optUser.get().getEmailAddress());
        return message;
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_JOB_CANCELLATION)
    private void informCancelJobRequested(JobMessage message) {

        DomainMessage infoRequest = DomainMessageFactory.createEmptyRequest(MessageID.REQUEST_JOB_CANCELLATION);
        infoRequest.set(MessageDataKeys.JOB_CANCEL_DATA, message);

        eventBusService.sendAsynchron(infoRequest);
    }
}
