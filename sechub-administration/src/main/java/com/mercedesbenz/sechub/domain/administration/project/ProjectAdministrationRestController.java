// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput.ProjectMetaData;
import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput.ProjectWhiteList;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminAssignsTemplateToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUnassignsTemplateFromProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminChangesProjectAccessLevel;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminChangesProjectDescription;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminCreatesProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDeleteProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminListsAllProjects;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminShowsProjectDetails;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminOrOwnerAssignsUserToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminOrOwnerUnassignsUserFromProject;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;

/**
 * The REST API for project administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
public class ProjectAdministrationRestController {

    @Autowired
    ProjectCreationService creationService;

    @Autowired
    ProjectChangeOwnerService assignOwnerToProjectService;

    @Autowired
    ProjectAssignUserService assignUserToProjectService;

    @Autowired
    ProjectUnassignUserService unassignUserToProjectService;

    @Autowired
    ProjectDeleteService deleteService;

    @Autowired
    ProjectDetailInformationService detailsInformationService;

    @Autowired
    ProjectDetailChangeService detailsChangeService;

    @Autowired
    ProjectChangeAccessLevelService projectAccessLevelChangeService;

    @Autowired
    ProjectRepository repository;

    @Autowired
    CreateProjectInputValidator validator;

    @Autowired
    ListProjectsService listProjectsService;

    @Autowired
    ProjectTemplateService projectTemplateService;

    /* @formatter:off */
	@UseCaseAdminCreatesProject(
			@Step(
				number = 1,
				name = "Rest call",
				needsRestDoc = true,
				description = "Administrator creates a new project by calling rest api"))
	@RequestMapping(path = AdministrationAPIConstants.API_CREATE_PROJECT, method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.CREATED)
	public void createProject(@RequestBody @Valid ProjectJsonInput input) {
		Set<URI> whiteListedURIs = new LinkedHashSet<>();
		Optional<ProjectWhiteList> whitelistOption = input.getWhiteList();

		if (whitelistOption.isPresent()) {
			ProjectWhiteList whiteList = whitelistOption.get();
			whiteListedURIs.addAll(whiteList.getUris());
		}

		ProjectMetaData metaData = new ProjectMetaData();
		if (input.getMetaData().isPresent()) {
			metaData = input.getMetaData().get();
		}

		/* @formatter:on */
        creationService.createProject(input.getName(), input.getDescription(), input.getOwner(), whiteListedURIs, metaData);
    }

    /* @formatter:off */
	@UseCaseAdminShowsProjectDetails(@Step(number = 1, name="Rest call", description = "Json returned containing details about project", needsRestDoc = true))
	@RequestMapping(path = AdministrationAPIConstants.API_SHOW_PROJECT_DETAILS, method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ProjectDetailInformation showProjectDetails(@PathVariable(name = "projectId") String projectId) {
		/* @formatter:on */
        return detailsInformationService.fetchDetails(projectId);
    }

    /* @formatter:off */
    @UseCaseAdminChangesProjectDescription(@Step(number = 1, name="Rest call", description = "Changes project description. Json returned containing details about changed project", needsRestDoc = true))
    @RequestMapping(path = AdministrationAPIConstants.API_CHANGE_PROJECT_DETAILS, method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ProjectDetailInformation changeProjectDescription(@PathVariable(name = "projectId") String projectId, @RequestBody ProjectJsonInput project) {
        /* @formatter:on */
        return detailsChangeService.changeProjectDescription(projectId, project);
    }

    /* @formatter:off */
	@UseCaseAdminListsAllProjects(@Step(number = 1, name = "Rest call", description = "All project ids of sechub are returned as json", needsRestDoc = true))
	@RequestMapping(path = AdministrationAPIConstants.API_LIST_ALL_PROJECTS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	public List<String> listProjects() {
		/* @formatter:on */
        return listProjectsService.listProjects();
    }

    /*
     * This is an out dated REST API and will removed in future - we provide same
     * functionality via management URL (but for owners and administrators). The old
     * API endpoint is only kept for compatibility reasons and works only for
     * administrators
     */
    @Deprecated(forRemoval = true)
    /* @formatter:off */
    @RequestMapping(path = AdministrationAPIConstants.API_OLD_ASSIGN_OWNER_TO_PROJECT, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public void changeProjectOwner(@PathVariable(name = "projectId") String projectId, @PathVariable(name = "userId") String userId) {
        /* @formatter:on */
        assignOwnerToProjectService.changeProjectOwner(userId, projectId);
    }

    /* @formatter:off */
    @Deprecated(forRemoval = true)
	@UseCaseAdminOrOwnerAssignsUserToProject(@Step(number = 1, name = "Rest call", description = "Administrator does call rest API to assign user", needsRestDoc = true))
	@RequestMapping(path = AdministrationAPIConstants.OLD_API_ASSIGN_USER_TO_PROJECT, method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void assignUserToProject(@PathVariable(name = "projectId") String projectId, @PathVariable(name = "userId") String userId) {
		/* @formatter:on */
        assignUserToProjectService.assignUserToProject(userId, projectId, true);
    }

    /* @formatter:off */
    @Deprecated(forRemoval = true)
	@UseCaseAdminOrOwnerUnassignsUserFromProject(@Step(number = 1, name = "Rest call", description = "Administrator does call rest API to unassign user", needsRestDoc = true))
	@RequestMapping(path = AdministrationAPIConstants.OLD_API_UNASSIGN_USER_TO_PROJECT, method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void unassignUserFromProject(@PathVariable(name = "projectId") String projectId, @PathVariable(name = "userId") String userId) {
		/* @formatter:on */
        unassignUserToProjectService.unassignUserFromProject(userId, projectId);
    }

    /* @formatter:off */
	@UseCaseAdminDeleteProject(@Step(number = 1, name = "Rest call", description = "Project will be deleted", needsRestDoc = true))
	@RequestMapping(path = AdministrationAPIConstants.API_DELETE_PROJECT, method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
	public void deleteProject(@PathVariable(name = "projectId") String projectId) {
		/* @formatter:on */
        deleteService.deleteProject(projectId);
    }

    /* @formatter:off */
    @UseCaseAdminChangesProjectAccessLevel(@Step(number = 1, name = "Rest call", description = "Admin does call REST API to change project access level", needsRestDoc = true))
    @RequestMapping(path = AdministrationAPIConstants.API_CHANGE_PROJECT_ACCESSLEVEL, method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void changeProjectAccessLevel(@PathVariable(name = "projectId") String projectId, @PathVariable(name = "projectAccessLevel") ProjectAccessLevel projectAccessLevel) {
        /* @formatter:on */
        projectAccessLevelChangeService.changeProjectAccessLevel(projectId, projectAccessLevel);
    }

    /* @formatter:off */
    @UseCaseAdminAssignsTemplateToProject(@Step(number = 1, name = "Rest call", description = "Admin does call REST API to assign a template to project", needsRestDoc = true))
    @RequestMapping(path = AdministrationAPIConstants.API_ASSIGN_TEMPLATE_TO_PROJECT, method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void assignTemplateToProject(@PathVariable(name = "projectId") String projectId, @PathVariable(name = "templateId") String templateId) {
        /* @formatter:on */
        projectTemplateService.assignTemplateToProject(templateId, projectId);
    }

    /* @formatter:off */
    @UseCaseAdminUnassignsTemplateFromProject(@Step(number = 1, name = "Rest call", description = "Admin does call REST API to unassign a template from project", needsRestDoc = true))
    @RequestMapping(path = AdministrationAPIConstants.API_UNASSIGN_TEMPLATE_FROM_PROJECT, method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void unassignTemplateFromProject(@PathVariable(name = "projectId") String projectId, @PathVariable(name = "templateId") String templateId) {
        /* @formatter:on */
        projectTemplateService.unassignTemplateFromProject(templateId, projectId);
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(validator);
    }
}
