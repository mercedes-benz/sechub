// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseSchedulerStartsJob;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class JobInformationDeleteService {

    private static final Logger LOG = LoggerFactory.getLogger(JobInformationDeleteService.class);

    @Autowired
    JobInformationRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @Validated
    @UseCaseSchedulerStartsJob(@Step(number = 5, name = "Update admin job info", description = "Deletes store info in admin domain when job is done."))
    public void delete(UUID jobUUID) {
        assertion.isValidJobUUID(jobUUID);

        LOG.debug("deleting job information for job with uuid:{}", jobUUID);
        repository.deleteJobInformationWithJobUUID(jobUUID);
    }

}
