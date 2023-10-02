// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.pds.PDSAPIConstants;
import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupConfig;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesAutoCleanupConfiguration;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesServerConfiguration;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminUpdatesAutoCleanupConfiguration;

import jakarta.annotation.security.RolesAllowed;

/**
 * The REST API for PDS jobs
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed({ PDSRoleConstants.ROLE_SUPERADMIN })
public class PDSAdminConfigRestController {

    @Autowired
    private PDSServerConfigurationService configurationService;

    @Autowired
    private PDSConfigService configService;

    @Validated
    @RequestMapping(path = PDSAPIConstants.API_SERVER_CONFIG, method = RequestMethod.GET)
    @UseCaseAdminFetchesServerConfiguration(@PDSStep(name = "rest call", description = "an admin fetches server configuration of PDS server(s).", number = 1))
    public PDSServerConfiguration getServerConfiguration() {
        return configurationService.getServerConfiguration();
    }

    /* @formatter:off */
    @UseCaseAdminFetchesAutoCleanupConfiguration(@PDSStep(number=1,name="Rest call",description="Administrator fetches auto cleanup configuration",needsRestDoc=true))
    @RequestMapping(path = PDSAPIConstants.API_AUTOCLEAN, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public PDSAutoCleanupConfig fetchAutoCleanupConfiguration() {
        /* @formatter:on */
        return configService.fetchAutoCleanupConfiguration();
    }

    /* @formatter:off */
    @UseCaseAdminUpdatesAutoCleanupConfiguration(@PDSStep(number=1,name="Rest call",description="Administrator changes auto cleanup configuration",needsRestDoc=true))
    @RequestMapping(path = PDSAPIConstants.API_AUTOCLEAN, method = RequestMethod.PUT, produces= {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateAutoCleanupConfiguration(@RequestBody  PDSAutoCleanupConfig config) {
        /* @formatter:on */
        configService.updateAutoCleanupConfiguration(config);
    }

}
