// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.monitoring;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.sechub.pds.PDSAPIConstants;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseAnonymousCheckAlive;

@RestController
public class PDSAnonymousCheckAliveRestController {

	/* @formatter:off */
	@UseCaseAnonymousCheckAlive(@PDSStep(name="rest call",description = "anybody - even anonymous - checks server alive.",number=1))
	@RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "check/alive", method = RequestMethod.HEAD)
	public void checkAlive() {
	    /* do nothing here - its just a HEAD request */
	}
	/* @formatter:on */
}
