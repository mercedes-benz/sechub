// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDeleteProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.transaction.Transactional;

@Service
public class ScanDeleteAnyAccessToProjectAtAllService {

    private static final Logger LOG = LoggerFactory.getLogger(ScanDeleteAnyAccessToProjectAtAllService.class);

    @Autowired
    ScanAccessRepository scanAccessRepository;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    LogSanitizer logSanitizer;

    @Transactional
    @UseCaseAdminDeleteProject(@Step(number = 7, name = "revoke any scan access from project"))
    public void deleteAnyAccessDataForProject(String projectId) {
        assertion.assertIsValidProjectId(projectId);

        scanAccessRepository.deleteAnyAccessForProject(projectId);

        LOG.info("Deleted any access at all for project:{}", logSanitizer.sanitize(projectId, 30));
    }

}
