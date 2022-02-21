// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.config;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupConfig;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAutoCleanupConfiguration;

/**
 * The rest api for administration config operations done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
public class AdministrationConfigRestController {

    @Autowired
    AdministrationConfigService configService;

    /* @formatter:off */
	@UseCaseAdminFetchesAutoCleanupConfiguration(@Step(number=1,name="Rest call",description="Administrator fetches auto cleanup configuration",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_ADMIN_FETCHES_AUTOCLEAN_CONFIG, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public AutoCleanupConfig fetchAutoCleanupConfiguration() {
		/* @formatter:on */
        return configService.fetchAutoCleanupConfiguration();
    }

    /* @formatter:off */
	@UseCaseAdminFetchesAutoCleanupConfiguration(@Step(number=1,name="Rest call",description="Administrator changes auto cleanup configuration",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_ADMIN_UPDATES_AUTOCLEAN_CONFIG, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public void updateAutoCleanupConfiguration(@RequestBody  AutoCleanupConfig config) {
	    /* @formatter:on */
        configService.updateAutoCleanup(config);
    }

}