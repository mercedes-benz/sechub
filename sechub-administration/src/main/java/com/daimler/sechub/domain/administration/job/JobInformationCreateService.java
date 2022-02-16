// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.job;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.JobMessage;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseSchedulerStartsJob;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class JobInformationCreateService {

    private static final Logger LOG = LoggerFactory.getLogger(JobInformationCreateService.class);

    @Autowired
    JobInformationRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @Validated
    @UseCaseSchedulerStartsJob(@Step(number = 4, name = "Store admin job info", description = "Fetches event about started job and store info in admin domain."))
    public void createByMessage(JobMessage message, JobStatus status) {
        String projectId = message.getProjectId();
        UUID jobUUID = message.getJobUUID();

        assertion.isValidProjectId(projectId);
        assertion.isValidJobUUID(jobUUID);

        LOG.debug("creating a new job information entry for project={}, job={}", projectId, jobUUID);

        JobInformation entity = new JobInformation();

        entity.setProjectId(projectId);
        entity.setJobUUID(jobUUID);
        entity.setConfiguration(message.getConfiguration());
        entity.setOwner(message.getOwner());
        entity.setSince(message.getSince());

        entity.setStatus(status);

        entity = repository.save(entity);

        LOG.debug("saved new job information entry uuid={} - for project={}, job={}, ", projectId, jobUUID, entity.getUUID());
    }

}
