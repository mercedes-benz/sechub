// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.config;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.pds.PDSAPIConstants;
import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.pds.usecase.UseCaseAdminFetchesServerConfiguration;

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
    private PDSServerConfigurationService executionService;

    @Validated
    @RequestMapping(path = "config/server", method = RequestMethod.GET)
    @UseCaseAdminFetchesServerConfiguration
    public PDSServerConfiguration getServerConfiguration() {
        return executionService.getServerConfiguration();
    }

}
