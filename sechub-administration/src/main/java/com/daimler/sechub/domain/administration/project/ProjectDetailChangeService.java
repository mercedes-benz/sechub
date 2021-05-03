// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorChangesProjectDescription;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

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
	@UseCaseAdministratorChangesProjectDescription(
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

        assertion.isValidProjectId(projectId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("changing project description for project: {}", logSanitizer.sanitize(projectId, 30));
        }

        Project project = projectRepository.findOrFailProject(projectId);

        project.description = description;

        Project storedProject = transactionService.saveInOwnTransaction(project);

        return new ProjectDetailInformation(storedProject);
    }
}
