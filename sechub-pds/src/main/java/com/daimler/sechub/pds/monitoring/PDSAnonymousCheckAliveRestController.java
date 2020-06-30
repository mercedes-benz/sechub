// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.monitoring;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.pds.PDSAPIConstants;
import com.daimler.sechub.pds.usecase.UseCaseAnonymousCheckAlive;

@RestController
public class PDSAnonymousCheckAliveRestController {

	/* @formatter:off */
	@UseCaseAnonymousCheckAlive
	@RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "check/alive", method = RequestMethod.HEAD)
	public void checkAlive() {
	    /* do nothing here - its just a HEAD request */
	}
	/* @formatter:on */
}
