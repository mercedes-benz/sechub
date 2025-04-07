// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.access;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminOrOwnerAssignsUserToProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScheduleGrantUserAccessToProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleGrantUserAccessToProjectService.class);

    @Autowired
    ScheduleAccessRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @UseCaseAdminOrOwnerAssignsUserToProject(@Step(number = 2, name = "Update schedule authorization parts"))
    public void grantUserAccessToProject(String userId, String projectId) {
        assertion.assertIsValidUserId(userId);
        assertion.assertIsValidProjectId(projectId);

        ScheduleAccess scheduleAccess = new ScheduleAccess(userId, projectId);
        Optional<ScheduleAccess> potentialAlreadyFound = repository.findById(scheduleAccess.getKey());
        if (potentialAlreadyFound.isPresent()) {
            LOG.debug("User {} has already access to {} so skipped", userId, projectId);
            return;
        }
        LOG.debug("User {} has now gained access to {}", userId, projectId);
        repository.save(scheduleAccess);
    }

}
