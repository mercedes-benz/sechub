// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorAddsExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorDisablesExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorEnablesExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorRemovesExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesExecutorConfigSetup;

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
    CreateProductExecutorConfigService createConfigService;

    @Autowired
    DeleteProductExecutorConfigService deleteConfigService;
    
    @Autowired
    UpdateProductExecutorConfigSetupService updateConfigSetupService;
    
    @Autowired
    FetchProductExecutorConfigSetupService fetchConfigSetupService;
    
    @Autowired
    ChangeEnableStateOfProductExecutorConfigService changeEnableStateConfigService;

    /* @formatter:off */
	@UseCaseAdministratorAddsExecutorConfiguration(
			@Step(
				number=1,
				name="Rest call",
				needsRestDoc=true,
				description="Administrator adds a new product executor configuration by calling REST API"))
	@RequestMapping(path = "executor/{productIdentifier}/{executorVersion}", method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.CREATED)
	public UUID addProductExecutorConfig(@RequestParam("productIdentifier")ProductIdentifier productIdentifier,@RequestParam("executorVersion") Integer executorVersion ) {
	   
	    return createConfigService.createProductExecutorConfig(productIdentifier, executorVersion);
	    
		/* @formatter:on */
    }

    /* @formatter:off */
	@UseCaseAdministratorRemovesExecutorConfiguration(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator deletes an existing product executor configuration by calling REST API"))
	@RequestMapping(path = "executor/{uuid}", method = RequestMethod.DELETE, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void removeProductExecutorConfig(@RequestParam("uuid")UUID uuid) {
	    deleteConfigService.deleteProductExecutorConfig(uuid);
	    /* @formatter:on */
    }
	
	/* @formatter:off */
	@UseCaseAdministratorUpdatesExecutorConfigSetup(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator updates setup for an existing product executor configuration by calling REST API"))
	@RequestMapping(path = "executor/{uuid}/setup", method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void updateProductExecutorConfigSetup(@RequestParam("uuid")UUID uuid, @RequestBody ProductExecutorConfigSetup setup) {
	    updateConfigSetupService.updateProductExecutorSetup(uuid,setup);
	    /* @formatter:on */
	}
	/* @formatter:off */
	@UseCaseAdministratorFetchesExecutorConfiguration(
	        @Step(
	                number=1,
	                name="Rest call",
	                needsRestDoc=true,
	                description="Administrator fetches setup of an existing product executor configuration by calling REST API"))
	@RequestMapping(path = "executor/{uuid}/setup", method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public ProductExecutorConfigSetup fechProductExecutorConfigSetup(@RequestParam("uuid")UUID uuid) {
	    return fetchConfigSetupService.fetchProductExecutorConfigSetup(uuid);
	    /* @formatter:on */
	}
	
	/* @formatter:off */
    @UseCaseAdministratorEnablesExecutorConfiguration(
            @Step(
                    number=1,
                    name="Rest call",
                    needsRestDoc=true,
                    description="Administrator enables an existing product executor configuration by calling REST API"))
    @RequestMapping(path = "executor/{uuid}/enable", method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public void enableProductExecutor(@RequestParam("uuid")UUID uuid) {
        changeEnableStateConfigService.enableProductExecutorConfig(uuid);
        /* @formatter:on */
    }
    
    /* @formatter:off */
    @UseCaseAdministratorDisablesExecutorConfiguration(
            @Step(
                    number=1,
                    name="Rest call",
                    needsRestDoc=true,
                    description="Administrator disables an existing product executor configuration by calling REST API"))
    @RequestMapping(path = "executor/{uuid}/disable", method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public void disableProductExecutor(@RequestParam("uuid")UUID uuid) {
        changeEnableStateConfigService.disableProductExecutorConfig(uuid);
        /* @formatter:on */
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
    }
}
