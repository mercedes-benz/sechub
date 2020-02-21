// SPDX-License-Identifier: MIT
package com.daimler.sechub.server.core;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.admin.status.UseCaseAdministratorChecksServerVersion;

@RestController
@EnableAutoConfiguration
@Profile({Profiles.ADMIN_ACCESS})
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ServerInfoAdministrationRestController {

	@Autowired
	private InfoService serverInfoService;

	/* @formatter:off */
	@UseCaseAdministratorChecksServerVersion(
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
