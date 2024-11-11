// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.access.ScanUserAccessToProjectValidationService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigAccessLevelService;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.sharedkernel.error.ForbiddenException;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ProjectIdValidation;

@Service
public class ScanAssertService {

    @Autowired
    UserContextService userContextService;

    @Autowired
    ScanUserAccessToProjectValidationService userAccessValidation;

    @Lazy
    @Autowired
    ScanProjectConfigAccessLevelService accessLevelService;

    @Autowired
    ProjectIdValidation projectIdValidation;

    public void assertUserHasAccessToReport(ScanReport report) {
        if (report == null) {
            throw new IllegalArgumentException("report may not be null");
        }
        assertUserHasAccessToProject(report.getProjectId());

    }

    public void assertUserHasAccessToProject(String projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId may not be null");
        }
        if (userContextService.isSuperAdmin()) {
            /* always access */
            return;
        }
        userAccessValidation.assertUserHasAccessToProject(projectId);

    }

    public void assertProjectIdValid(String projectId) {
        AssertValidation.assertValid(projectId, projectIdValidation);
    }

    public void assertProjectAllowsReadAccess(String projectId) {
        assertProjectIdValid(projectId);

        if (!accessLevelService.isReadAllowed(projectId)) {
            throw new ForbiddenException("Project " + projectId + " does currently not allow read access.");
        }
    }

    public void assertProjectAllowsWriteAccess(String projectId) {
        assertProjectIdValid(projectId);

        if (!accessLevelService.isWriteAllowed(projectId)) {
            throw new ForbiddenException("Project " + projectId + " does currently not allow write access.");
        }
    }

}
