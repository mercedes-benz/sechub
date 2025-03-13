// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.messaging.JobMessage;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseSchedulerStartsJob;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserCreatesNewJob;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class JobInformationCreateService {

    private static final Logger LOG = LoggerFactory.getLogger(JobInformationCreateService.class);

    @Autowired
    JobInformationRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @Validated
    @UseCaseSchedulerStartsJob(@Step(number = 4, name = "Store admin job info", description = "Fetches event about started job and store info in admin domain."))
    @UseCaseUserCreatesNewJob(@Step(number = 3, name = "Store admin job info", description = "Fetches event about created job and store info in admin domain."))
    public void createOrUpdateByMessage(JobMessage message, JobStatus status) {
        String projectId = message.getProjectId();
        UUID jobUUID = message.getJobUUID();

        assertion.assertIsValidProjectId(projectId);
        assertion.assertIsValidJobUUID(jobUUID);

        LOG.debug("Creating a new job information entry for project: {}, SecHub job: {}", projectId, jobUUID);

        JobInformation jobInformation = null;

        Optional<JobInformation> existingInfo = repository.findById(jobUUID);
        if (existingInfo.isPresent()) {
            jobInformation = existingInfo.get();

            LOG.warn("There was an existing information entity about SecHub job: {}. The entity will be reused and updated.", jobUUID);

        } else {
            jobInformation = new JobInformation(jobUUID);
        }

        jobInformation.setProjectId(projectId);
        jobInformation.setOwner(message.getOwner());
        jobInformation.setSince(message.getSince());

        jobInformation.setStatus(status);

        jobInformation = repository.save(jobInformation);

        LOG.debug("Saved job information entry for project: {}, SecHub job: {}, ", projectId, jobUUID);
    }

}
