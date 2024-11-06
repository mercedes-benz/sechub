package com.mercedesbenz.sechub.domain.administration.project;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.project.UseCaseGetProjectDataList;

import jakarta.annotation.security.RolesAllowed;

@RestController
public class ProjectRestController {

    final private ProjectService projectService;
    final private UserContextService userContextService;

    public ProjectRestController(ProjectService projectService, UserContextService userContextService) {
        this.projectService = projectService;
        this.userContextService = userContextService;
    }

    @UseCaseGetProjectDataList(@Step(number = 1, name = "Rest API call to get Projects with information", description = "Rest api call to get projects with details", needsRestDoc = true))
    @RequestMapping(path = AdministrationAPIConstants.API_GET_PROJECTS, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @RolesAllowed({ RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER })
    public ProjectData[] getProjectDataList() {
        String userId = userContextService.getUserId();
        return projectService.getProjectDataList(userId);
    }
}
