// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import javax.annotation.security.RolesAllowed;

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

import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorCreatesExecutionProfile;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorDeletesExecutionProfile;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutorConfigList;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesExecutorConfig;

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
	@UseCaseAdministratorCreatesExecutionProfile(
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
	@UseCaseAdministratorDeletesExecutionProfile(
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

    /* @formatter:off */
	@UseCaseAdministratorUpdatesExecutorConfig(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator updates setup for an existing product executor configuration by calling REST API"))
	@RequestMapping(path = "execution/profile/{id}", method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void udpateProfile(@PathVariable("id")String profileId, @RequestBody ProductExecutionProfile profile) {
	    updateService.updateProductExecutorSetup(profileId,profile);
	    /* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdministratorFetchesExecutorConfigList(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator fetches lsit of existing product executor configurations by calling REST API, will not contain setup information"))
	@RequestMapping(path = "execution/profiles", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public ProductExecutionProfilesList fetchProfileList() {
	    return fetchListService.fetchProductExecutorConfigList();
	    /* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdministratorFetchesExecutorConfiguration(
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
