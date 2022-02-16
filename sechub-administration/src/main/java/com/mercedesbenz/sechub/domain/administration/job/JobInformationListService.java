// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseAdminListsAllRunningJobs;

@Service
public class JobInformationListService {

    @Autowired
    JobInformationRepository repository;

    @UseCaseAdminListsAllRunningJobs(@Step(number = 2, name = "Fetchjob information from database", description = "Fetches stored job information from administration database."))
    public List<JobInformation> fetchRunningJobs() {

        return repository.findAllRunningJobs();
    }

}
