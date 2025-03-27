// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.access;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.access.ScanAccess.ProjectAccessCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.SecurityLogType;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;

@Service
public class ScanUserAccessToProjectValidationService {

    @Autowired
    ScanAccessRepository accessRepository;

    @Autowired
    UserContextService userContextService;

    @Autowired
    SecurityLogService securityLogService;

    @Autowired
    LogSanitizer logSanitizer;

    /**
     * Assert user logged in has access to project
     *
     * @param projectId
     */
    public void assertUserHasAccessToProject(String projectId) {
        if (userContextService.isSuperAdmin()) {
            /* a super admin has always access */
            return;
        }
        String userId = userContextService.getUserId();

        ProjectAccessCompositeKey key = new ProjectAccessCompositeKey(userId, projectId);
        Optional<ScanAccess> project = accessRepository.findById(key);
        if (!project.isPresent()) {
            securityLogService.log(SecurityLogType.POTENTIAL_INTRUSION, "Denied access for user '{}' at project '{}' in domain 'scan'.", userId,
                    logSanitizer.sanitize(projectId, 30));
            // we say "... or you have no access - just to obfuscate... so it's not clear to
            // bad guys they got a target...
            throw new NotFoundException("Project " + projectId + " does not exist, or you have no access.");
        }
    }

}
