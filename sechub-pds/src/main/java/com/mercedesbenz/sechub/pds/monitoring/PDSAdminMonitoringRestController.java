// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.pds.PDSAPIConstants;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseAdminFetchesMonitoringStatus;

import jakarta.annotation.security.RolesAllowed;

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
public class PDSAdminMonitoringRestController {

    @Autowired
    private PDSMonitoringStatusService monitoringStatusService;

    @Validated
    @RequestMapping(path = "monitoring/status", method = RequestMethod.GET)
    @UseCaseAdminFetchesMonitoringStatus(@PDSStep(name = "rest call", description = "admin fetches monitoring status by REST API", number = 3))
    public PDSMonitoring getMonitoringStatus() {
        return monitoringStatusService.getMonitoringStatus();
    }

}
