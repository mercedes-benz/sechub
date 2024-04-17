// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserRequestsNewApiToken;

/**
 * Request new api token by a given onetimetoken. Can be done anonymous
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
public class AnonymousUserRequestNewApiTokenRestController {

    @Autowired
    private AnonymousUserRequestsNewApiTokenService newApiTokenService;

    /* @formatter:off */
	@CrossOrigin /* to allow call from getsechub.detss and maybe other sites using javascript */
	@UseCaseUserRequestsNewApiToken(@Step(number=1, name="Rest API call",description="Rest api called to request new user api token. Normally done by user itself",needsRestDoc=true))
	@RequestMapping(path = AdministrationAPIConstants.API_REQUEST_NEW_APITOKEN, method = RequestMethod.POST)
	public void anonymousRequestToGetNewApiTokenForUserEmailAddress(@PathVariable(name="emailAddress") String emailAddress) {
		/* @formatter:on */
        newApiTokenService.anonymousRequestToGetNewApiTokenForUserEmailAddress(emailAddress);
    }

}
