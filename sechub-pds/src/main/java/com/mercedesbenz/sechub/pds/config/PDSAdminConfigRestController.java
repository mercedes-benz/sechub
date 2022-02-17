// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.pds.PDSAPIConstants;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesServerConfiguration;

/**
 * The REST API for PDS jobs
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(PDSAPIConstants.API_ADMIN)
@RolesAllowed({ PDSRoleConstants.ROLE_SUPERADMIN })
public class PDSAdminConfigRestController {

    @Autowired
    private PDSServerConfigurationService configurationService;

    @Validated
    @RequestMapping(path = "config/server", method = RequestMethod.GET)
    @UseCaseAdminFetchesServerConfiguration(@PDSStep(name = "rest call", description = "an admin fetches server configuration of PDS server(s).", number = 1))
    public PDSServerConfiguration getServerConfiguration() {
        return configurationService.getServerConfiguration();
    }

}
