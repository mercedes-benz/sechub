// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

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

import com.mercedesbenz.sechub.sharedkernel.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminAssignsExecutionProfileToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminCreatesExecutionProfile;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesExecutionProfile;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesExecutionProfile;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesExecutionProfileList;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUnassignsExecutionProfileFromProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUpdatesExecutionProfile;

import jakarta.annotation.security.RolesAllowed;

/**
 * The REST API for project executor config done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@RequestMapping(APIConstants.API_ADMINISTRATION + "config/")
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
public class ProductExecutionProfileRestController {

    @Autowired
    CreateProductExecutionProfileService createService;

    @Autowired
    DeleteProductExecutionProfileService deleteService;

    @Autowired
    FetchProductExecutionProfileListService fetchListService;

    @Autowired
    UpdateProductExecutionProfileService updateService;

    @Autowired
    FetchProductExecutionProfileService fetchService;

    /* @formatter:off */
	@UseCaseAdminCreatesExecutionProfile(
			@Step(
				number=1,
				name="Rest call",
				needsRestDoc=true,
				description="Administrator adds a new product execution profile by calling REST API"))
	@RequestMapping(path="execution/profile/{id}",method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.CREATED)
	public void createProfile(@PathVariable("id")String profileId, @RequestBody ProductExecutionProfile profileFromuser) {
	    createService.createProductExecutionProfile(profileId,profileFromuser);
		/* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdminDeletesExecutionProfile(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator deletes an existing product execution profile by calling REST API"))
	@RequestMapping(path = "execution/profile/{id}", method = RequestMethod.DELETE, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void deleteProfile(@PathVariable("id")String profileId) {
	    deleteService.deleteProductExecutionProfile(profileId);
	    /* @formatter:on */
    }

    @UseCaseAdminUpdatesExecutionProfile(@Step(number = 1, name = "Rest call", needsRestDoc = true, description = "Administrator updates existing profile by calling REST API"))
    @RequestMapping(path = "execution/profile/{id}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseStatus(HttpStatus.OK)
    public void udpateProfile(@PathVariable("id") String profileId, @RequestBody ProductExecutionProfile profile) {
        updateService.updateExecutionProfile(profileId, profile);
        /* @formatter:on */
    }

    @UseCaseAdminAssignsExecutionProfileToProject(@Step(number = 1, name = "Rest call", needsRestDoc = true, description = "Administrator adds profile relation to project by calling REST API"))
    @RequestMapping(path = "execution/profile/{profileId}/project/{projectId}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseStatus(HttpStatus.CREATED)
    public void addProjectToProfile(@PathVariable("profileId") String profileId, @PathVariable("projectId") String projectId) {
        updateService.addProjectToProfileRelation(profileId, projectId);
        /* @formatter:on */
    }

    @UseCaseAdminUnassignsExecutionProfileFromProject(@Step(number = 1, name = "Rest call", needsRestDoc = true, description = "Administrator removes profile relation to project by calling REST API"))
    @RequestMapping(path = "execution/profile/{profileId}/project/{projectId}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseStatus(HttpStatus.OK)
    public void removeProjectFromProfile(@PathVariable("profileId") String profileId, @PathVariable("projectId") String projectId) {
        updateService.removeProjectToProfileRelation(profileId, projectId);
        /* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdminFetchesExecutionProfileList(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator fetches lsit of all available execution profiles by calling REST API"))
	@RequestMapping(path = "execution/profiles", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public ProductExecutionProfilesList fetchProfileList() {
	    return fetchListService.fetchProductExecutionProfileList();
	    /* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdminFetchesExecutionProfile(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator fetches setup of an existing product executor configuration by calling REST API"))
	@RequestMapping(path = "execution/profile/{id}", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public ProductExecutionProfile fechProductExecutorConfigSetup(@PathVariable("id")String profileId) {
	    return fetchService.fetchProductExecutorConfig(profileId);
	    /* @formatter:on */
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
    }
}
