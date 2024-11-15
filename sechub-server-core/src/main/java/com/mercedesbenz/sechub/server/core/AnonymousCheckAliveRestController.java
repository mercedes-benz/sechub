// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.anonymous.UseCaseAnonymousCheckAlive;

@RestController
public class AnonymousCheckAliveRestController {

    /* @formatter:off */
	@UseCaseAnonymousCheckAlive(
			@Step(
				number=1,
				name="REST API call",
				needsRestDoc=true,
				description="An anonymous user checks if the server is alive and running using the REST API"))
	@RequestMapping(path = APIConstants.API_ANONYMOUS + "check/alive", method = {RequestMethod.GET, RequestMethod.HEAD}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public String checkAlive() {
		/* empty result, only HTTP STATUS 200 OK is of interest */
	    return "";
	}
	/* @formatter:on */
}
