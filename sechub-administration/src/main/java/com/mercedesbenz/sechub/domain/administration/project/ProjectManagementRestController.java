// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminOrOwnerAssignsUserToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminOrOwnerChangesProjectOwner;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminOrOwnerUnassignsUserFromProject;

import jakarta.annotation.security.RolesAllowed;

/**
 * The REST API for project administration done by a super admin or a project
 * owner
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed({ RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER })
public class ProjectManagementRestController {

    @Autowired
    ProjectChangeOwnerService assignOwnerToProjectService;

    @Autowired
    ProjectAssignUserService assignUserToProjectService;

    @Autowired
    ProjectUnassignUserService unassignUserToProjectService;

    /* @formatter:off */
    @UseCaseAdminOrOwnerChangesProjectOwner(@Step(number = 1, name = "Rest call", description = "Administrator does call rest API to set new project owner", needsRestDoc=true))
    @RequestMapping(path = AdministrationAPIConstants.API_ASSIGN_OWNER_TO_PROJECT, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public void changeProjectOwner(@PathVariable(name = "projectId") String projectId, @PathVariable(name = "userId") String userId) {
        /* @formatter:on */
        assignOwnerToProjectService.changeProjectOwner(userId, projectId);
    }

    @UseCaseAdminOrOwnerAssignsUserToProject(@Step(number = 1, name = "Rest call", description = "Administrator does call rest API to assign user", needsRestDoc = true))
    @RequestMapping(path = AdministrationAPIConstants.API_ASSIGN_USER_TO_PROJECT, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseStatus(HttpStatus.OK)
    public void assignUserToProject(@PathVariable(name = "projectId") String projectId, @PathVariable(name = "userId") String userId) {
        /* @formatter:on */
        assignUserToProjectService.assignUserToProject(userId, projectId, true);
    }

    /* @formatter:off */
    @UseCaseAdminOrOwnerUnassignsUserFromProject(@Step(number = 1, name = "Rest call", description = "Administrator does call rest API to unassign user", needsRestDoc = true))
    @RequestMapping(path = AdministrationAPIConstants.API_UNASSIGN_USER_TO_PROJECT, method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public void unassignUserFromProject(@PathVariable(name = "projectId") String projectId, @PathVariable(name = "userId") String userId) {
        /* @formatter:on */
        unassignUserToProjectService.unassignUserFromProject(userId, projectId);
    }

}
