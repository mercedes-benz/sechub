// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.autocleanup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;

/**
 * Contains additional rest call functionality for integration tests on
 * administration domain
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestAdministrationRestController {

    @Autowired
    private AdministrationConfigService administrationConfigService;

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/autocleanup/inspection/administration/days", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public long fetchScheduleAutoCleanupConfiguredDays() {
        return administrationConfigService.getAutoCleanupInDays();
    }

}
