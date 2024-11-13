// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminShowsProjectDetails;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectDetailInformationService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectDetailInformationService.class);

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
	@UseCaseAdminShowsProjectDetails(
			@Step(
				number = 2,
				name = "Service fetches project details.",
				description = "The service will fetch project details"))
	/* @formatter:on */
    public ProjectDetailInformation fetchDetails(String projectId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetching project details for project:{}", logSanitizer.sanitize(projectId, 30));
        }

        assertion.assertIsValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);

        return new ProjectDetailInformation(project);
    }
}
