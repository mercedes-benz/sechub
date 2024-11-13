// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminChangesProjectDescription;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectDetailChangeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectDetailChangeService.class);

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    ProjectTransactionService transactionService;

    /* @formatter:off */
	@UseCaseAdminChangesProjectDescription(
			@Step(
				number = 2,
				name = "Service changes project description.",
				description = "The service will change project description."))
	/* @formatter:on */
    public ProjectDetailInformation changeProjectDescription(String projectId, ProjectJsonInput projectJson) {
        String description = projectJson.getDescription();

        if (description == null) {
            throw new NotAcceptableException("description field has to be set");
        }

        assertion.assertIsValidProjectId(projectId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("changing project description for project: {}", logSanitizer.sanitize(projectId, 30));
        }

        Project project = projectRepository.findOrFailProject(projectId);

        project.description = description;

        Project storedProject = transactionService.saveInOwnTransaction(project);

        return new ProjectDetailInformation(storedProject);
    }
}
