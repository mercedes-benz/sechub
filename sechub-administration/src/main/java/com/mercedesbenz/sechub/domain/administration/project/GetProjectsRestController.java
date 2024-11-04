package com.mercedesbenz.sechub.domain.administration.project;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.project.UseCaseGetProjects;

import jakarta.annotation.security.RolesAllowed;

@RestController
public class GetProjectsRestController {

    final private GetProjectsService getProjectsService;
    final private UserContextService userContextService;

    public GetProjectsRestController(GetProjectsService getProjectsService, UserContextService userContextService) {
        this.getProjectsService = getProjectsService;
        this.userContextService = userContextService;
    }

    @UseCaseGetProjects(@Step(number = 1, name = "Rest API call", description = "Rest api called to get projects with details", needsRestDoc = true))
    @RequestMapping(path = AdministrationAPIConstants.API_GET_PROJECTS, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @RolesAllowed({ RoleConstants.ROLE_USER, RoleConstants.ROLE_SUPERADMIN, RoleConstants.ROLE_OWNER })
    public String[] getProjects() {
        String userId = userContextService.getUserId();
        return getProjectsService.userListProjects(userId);
    }
}
