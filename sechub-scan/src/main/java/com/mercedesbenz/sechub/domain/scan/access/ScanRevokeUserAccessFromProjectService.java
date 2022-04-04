// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.access.ScanAccess.ProjectAccessCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUnassignsUserFromProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScanRevokeUserAccessFromProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ScanRevokeUserAccessFromProjectService.class);

    @Autowired
    ScanAccessRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @UseCaseAdminUnassignsUserFromProject(@Step(number = 2, name = "Update authorization parts"))
    public void revokeUserAccessFromProject(String userId, String projectId) {
        assertion.assertIsValidUserId(userId);
        assertion.assertIsValidProjectId(projectId);

        ProjectAccessCompositeKey id = new ProjectAccessCompositeKey(userId, projectId);
        repository.deleteById(id);

        LOG.info("Revoked access to project:{} for user:{}", projectId, userId);
    }

}
