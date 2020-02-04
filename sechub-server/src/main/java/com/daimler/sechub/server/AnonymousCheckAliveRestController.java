// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.sharedkernel.APIConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.anonymous.UseCaseAnonymousCheckAlive;

@RestController
public class AnonymousCheckAliveRestController {

	/* @formatter:off */
	@UseCaseAnonymousCheckAlive(
			@Step(
				number=1,
				name="REST API call",
				needsRestDoc=true,
				description="An anonymous user checks if the server is alive and running using the REST API"))
	@RequestMapping(path = APIConstants.API_ANONYMOUS + "check/alive", method = RequestMethod.HEAD)
	public void getServerVersion() {
	}

}
