// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput.ProjectMetaData;
import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput.ProjectWhiteList;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectMetaData;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectWhitelist;

import jakarta.annotation.security.RolesAllowed;

/**
 * The rest api for user administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
public class ProjectUpdateAdministrationRestController {

    @Autowired
    ProjectRepository repository;

    @Autowired
    private UpdateProjectInputValidator validator;

    @Autowired
    private ProjectUpdateWhitelistService updateProjectWhitelistService;

    @Autowired
    private ProjectUpdateMetaDataEntityService updateProjectMetaDataService;

    /* @formatter:off */
	@UseCaseUpdateProjectWhitelist(@Step(number=1,name="Rest call",description="White list will be updated",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_UPDATE_PROJECT_WHITELIST, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	public void updateProjectWhitelist(@Validated @RequestBody ProjectJsonInput input, @PathVariable(name="projectId") String projectId) {
		/* @formatter:on */
        Optional<ProjectWhiteList> projectWhiteList = input.getWhiteList();
        List<URI> whiteList;
        if (projectWhiteList.isPresent()) {
            ProjectWhiteList r = projectWhiteList.get();
            whiteList = r.getUris();
        } else {
            whiteList = Collections.emptyList();
        }
        updateProjectWhitelistService.updateProjectWhitelist(projectId, whiteList);
    }

    @UseCaseUpdateProjectMetaData(@Step(number = 1, name = "Rest call", description = "MetaData will be updated", needsRestDoc = true))
    @RequestMapping(path = AdministrationAPIConstants.API_UPDATE_PROJECT_METADATA, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public void updateProjectMetaData(@Validated @RequestBody ProjectJsonInput input, @PathVariable(name = "projectId") String projectId) {
        /* @formatter:on */
        Optional<ProjectMetaData> projectMetaData = input.getMetaData();
        if (!projectMetaData.isPresent()) {
            return;
        }

        ProjectMetaData metaData = projectMetaData.get();

        updateProjectMetaDataService.updateProjectMetaData(projectId, metaData);
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(validator);
    }
}
