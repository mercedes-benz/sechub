// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static com.mercedesbenz.sechub.sharedkernel.security.APIConstants.API_PROJECTS;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.project.UseCaseGetAssignedProjectDataList;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;

import jakarta.annotation.security.RolesAllowed;

@RestController
public class ProjectRestController {

    private final ProjectService projectService;
    private final UserContextService userContextService;

    public ProjectRestController(ProjectService projectService, UserContextService userContextService) {
        this.projectService = projectService;
        this.userContextService = userContextService;
    }

    @UseCaseGetAssignedProjectDataList(@Step(number = 1, name = "Rest API call to get Projects with information", description = "Rest api call to get projects with details", needsRestDoc = true))
    @RequestMapping(path = API_PROJECTS, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @RolesAllowed({ RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER })
    public List<ProjectData> getAssignedProjectDataList() {
        String userId = userContextService.getUserId();
        return projectService.getAssignedProjectDataList(userId);
    }
}
