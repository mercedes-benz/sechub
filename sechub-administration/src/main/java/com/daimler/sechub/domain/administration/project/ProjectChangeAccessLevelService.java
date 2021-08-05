// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorChangesProjectAccessLevel;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectChangeAccessLevelService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectChangeAccessLevelService.class);

    @Autowired
    DomainMessageService eventBus;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    ProjectTransactionService transactionService;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
	@UseCaseAdministratorChangesProjectAccessLevel(
			@Step(
					number = 2,
					name = "Change access level",
					description = "The service will change the access level and trigger event"))
	/* @formatter:on */
    public void changeProjectAccessLevel(String projectId, ProjectAccessLevel wantedLevel) {
	    /* audit */
        auditLogService.log("triggers for project:{} an access level change to '{}'", logSanitizer.sanitize(projectId, 30), wantedLevel);

        /* validate*/
        notNull(wantedLevel, "project access level may not be null!");
        assertion.isValidProjectId(projectId);

        Project project = projectRepository.findOrFailProject(projectId);

        ProjectAccessLevel formerAccessLevel = project.getAccessLevel();
        if (wantedLevel.equals(formerAccessLevel)) {
            LOG.info("Access level is already '{}' for project:{}, so cancel operation", formerAccessLevel, project.getId());
            return;
        }

        /* change */
        project.accessLevel = wantedLevel;
        transactionService.saveInOwnTransaction(project);

        /* trigger event */
        sendProjectAccessLevelChangedEvent(project, formerAccessLevel);
    }

    @IsSendingAsyncMessage(MessageID.PROJECT_ACCESS_LEVEL_CHANGED)
    private void sendProjectAccessLevelChangedEvent(Project project, ProjectAccessLevel formerAccessLevel) {

        DomainMessage request = new DomainMessage(MessageID.PROJECT_ACCESS_LEVEL_CHANGED);

        ProjectMessage projectData = new ProjectMessage();
        projectData.setProjectId(project.id);
        projectData.setFormerAccessLevel(formerAccessLevel);
        projectData.setNewAccessLevel(project.getAccessLevel());

        request.set(MessageDataKeys.PROJECT_ACCESS_LEVEL_CHANGE_DATA, projectData);

        eventBus.sendAsynchron(request);
    }

}
