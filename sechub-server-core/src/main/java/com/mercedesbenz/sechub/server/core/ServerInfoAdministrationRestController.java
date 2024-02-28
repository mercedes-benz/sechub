// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import javax.annotation.security.RolesAllowed;

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
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.status.UseCaseAdminFetchesServerRuntimeData;

@RestController
@EnableAutoConfiguration
@Profile({ Profiles.ADMIN_ACCESS })
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ServerInfoAdministrationRestController {

    @Autowired
    private InfoService serverInfoService;

    /* @formatter:off */
	@UseCaseAdminFetchesServerRuntimeData(
			@Step(
					number=1,
					name="REST API Call",
					description="Administrator wants to fetch server runtime data. This data contains for example the server version",
					needsRestDoc=true))
	@RequestMapping(path = APIConstants.API_ADMINISTRATION+ "info/server", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ServerRuntimeData getServerRuntimeData() {
		/* @formatter:on */
        return serverInfoService.getServerRuntimeData();
    }
}
