// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import java.util.UUID;

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

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminCreatesExecutorConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesExecutorConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesExecutorConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesExecutorConfigurationList;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUpdatesExecutorConfig;

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
public class ProductExecutorConfigRestController {

    @Autowired
    CreateProductExecutorConfigService createService;

    @Autowired
    DeleteProductExecutorConfigService deleteService;

    @Autowired
    FetchProductExecutorConfigListService fetchListService;

    @Autowired
    UpdateProductExecutorConfigService updateService;

    @Autowired
    FetchProductExecutorConfigService fetchService;

    /* @formatter:off */
	@UseCaseAdminCreatesExecutorConfiguration(
			@Step(
				number=1,
				name="Rest call",
				needsRestDoc=true,
				description="Administrator adds a new product executor configuration by calling REST API"))
	@RequestMapping(path = "executor", method = RequestMethod.POST, produces= {MediaType.TEXT_PLAIN_VALUE})
	@ResponseStatus(HttpStatus.CREATED)
	public String addProductExecutorConfig(@RequestBody ProductExecutorConfig configFromUser) {
	    return createService.createProductExecutorConfig(configFromUser);
		/* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdminDeletesExecutorConfiguration(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator deletes an existing product executor configuration by calling REST API"))
	@RequestMapping(path = "executor/{uuid}", method = RequestMethod.DELETE, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void removeProductExecutorConfig(@PathVariable("uuid")UUID uuid) {
	    deleteService.deleteProductExecutorConfig(uuid);
	    /* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdminUpdatesExecutorConfig(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator updates setup for an existing product executor configuration by calling REST API"))
	@RequestMapping(path = "executor/{uuid}", method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void updateProductExecutorConfigSetup(@PathVariable("uuid")UUID uuid, @RequestBody ProductExecutorConfig setup) {
	    updateService.updateProductExecutorSetup(uuid,setup);
	    /* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdminFetchesExecutorConfigurationList(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator fetches list of existing product executor configurations by calling REST API, will not contain setup information"))
	@RequestMapping(path = "executors", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public ProductExecutorConfigList fetchProductExecutorConfigurationsAsList() {
	    return fetchListService.fetchProductExecutorConfigList();
	    /* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdminFetchesExecutorConfiguration(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator fetches setup of an existing product executor configuration by calling REST API"))
	@RequestMapping(path = "executor/{uuid}", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public ProductExecutorConfig fetchProductExecutorConfigSetup(@PathVariable("uuid")UUID uuid) {
	    return fetchService.fetchProductExecutorConfig(uuid);
	    /* @formatter:on */
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
    }
}
