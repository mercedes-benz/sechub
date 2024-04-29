// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDeleteProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.transaction.Transactional;

@Service
public class ScheduleDeleteAllProjectAcessService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleDeleteAllProjectAcessService.class);

    @Autowired
    ScheduleAccessRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @Transactional
    @UseCaseAdminDeleteProject(@Step(number = 6, name = "Update authorization parts - remove entries for deleted project"))
    public void deleteAnyAccessDataForProject(String projectId) {
        assertion.assertIsValidProjectId(projectId);

        repository.deleteAnyAccessForProject(projectId);

        LOG.info("Removed any access entry for project:{}", projectId);
    }

}
