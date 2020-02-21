// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.sharedkernel.APIConstants;

@RestController
public class AnonymousCheckAliveRestController {

	@RequestMapping(path = APIConstants.API_ANONYMOUS + "check/alive", method = RequestMethod.HEAD)
	public void getServerVersion() {
	}

}
