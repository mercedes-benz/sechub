// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUnassignsUserFromProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScheduleRevokeUserAccessFromProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleRevokeUserAccessFromProjectService.class);

    @Autowired
    ScheduleAccessRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @UseCaseAdminUnassignsUserFromProject(@Step(number = 2, name = "Update authorization parts"))
    public void revokeUserAccessFromProject(String userId, String projectId) {
        assertion.isValidUserId(userId);
        assertion.isValidProjectId(projectId);

        ProjectAccessCompositeKey id = new ProjectAccessCompositeKey(userId, projectId);
        repository.deleteById(id);

        LOG.info("Revoked access to project:{} for user:{}", projectId, userId);
    }

}
