// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserClicksLinkToGetNewAPIToken;

/**
 * The rest api for users to get new api token by a wellknwon one time token
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
public class AnonymousUserGetApiTokenByOneTimeTokenRestController {

    @Autowired
    AnonymousUserGetAPITokenByOneTimeTokenService createUserAPITokenByOneTimeTokenService;

    /* @formatter:off */
	@UseCaseUserClicksLinkToGetNewAPIToken(@Step(number=1,name="Rest call",description="User opens url by browser",needsRestDoc=true))
	@GetMapping(path = AdministrationAPIConstants.API_FETCH_NEW_API_TOKEN_BY_ONE_WAY_TOKEN+"/{oneTimeToken}",
				produces= {MediaType.TEXT_PLAIN_VALUE})
	@ResponseBody
	public String getNewAPITokenByOneTimeToken(@PathVariable(name = "oneTimeToken", required = true) String oneTimeToken) {
		/* @formatter:on */
        return createUserAPITokenByOneTimeTokenService.createNewAPITokenForUserByOneTimeToken(oneTimeToken);
    }
}
