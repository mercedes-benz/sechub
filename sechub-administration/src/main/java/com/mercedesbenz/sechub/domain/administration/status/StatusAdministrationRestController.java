// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.status;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.status.UseCaseAdminListsStatusInformation;

import jakarta.annotation.security.RolesAllowed;

/**
 * The rest API for status administration done by a super admin.
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
public class StatusAdministrationRestController {

    @Autowired
    ListStatusService listStatusService;

    /* @formatter:off */
	@UseCaseAdminListsStatusInformation(@Step(number=1,name="Rest call",description="Administrator wants to list status information about sechub",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_SCHEDULER_GET_STATUS, method = RequestMethod.GET, produces= {MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public List<StatusEntry> listStatusInformation() {
		/* @formatter:on */
        return listStatusService.fetchAllStatusEntries();
    }

}