// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import jakarta.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.sharedkernel.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.status.UseCaseAdminChecksServerVersion;

@RestController
@EnableAutoConfiguration
@Profile({ Profiles.ADMIN_ACCESS })
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ServerInfoAdministrationRestController {

    @Autowired
    private InfoService serverInfoService;

    /* @formatter:off */
	@UseCaseAdminChecksServerVersion(
			@Step(
					number=1,
					name="REST API Call",
					description="Administrator wants to get the server version of SecHub",
					needsRestDoc=true))
	@RequestMapping(path = APIConstants.API_ADMINISTRATION+ "info/version", method = RequestMethod.GET, produces = { MediaType.TEXT_PLAIN_VALUE })
	@ResponseBody
	public String getServerVersion() {
		/* @formatter:on */
        return serverInfoService.getVersionAsString();
    }
}
