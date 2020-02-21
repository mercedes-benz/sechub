// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

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

@RestController
@EnableAutoConfiguration
@Profile(Profiles.ADMIN_ACCESS)
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ServerInfoAdministrationRestController {

	@Autowired
	private InfoService serverInfoService;

	@RequestMapping(path = APIConstants.API_ADMINISTRATION+ "info/version", method = RequestMethod.GET, produces = { MediaType.TEXT_PLAIN_VALUE })
	@ResponseBody
	public String getServerVersion() {
		return serverInfoService.getVersionAsString();
	}

}
