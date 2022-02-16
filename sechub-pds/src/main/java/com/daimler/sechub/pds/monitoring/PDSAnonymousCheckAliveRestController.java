// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.monitoring;

import org.springframework.http.MediaType;
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
	@RequestMapping(path = PDSAPIConstants.API_ANONYMOUS + "check/alive", method = {RequestMethod.GET, RequestMethod.HEAD}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public String checkAlive() {
	    /* empty result, only HTTP STATUS 200 OK is of interest */
	    return "";
	}
	/* @formatter:on */
}
