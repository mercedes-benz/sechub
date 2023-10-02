// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserSignup;

import jakarta.validation.Valid;

/**
 * Self registration rest controller - anonymous access possible
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@EnableAutoConfiguration
public class AnonymousSignupRestController {

    @Autowired
    private AnonymousSignupCreateService signupService;

    @Autowired
    private SignupJsonInputValidator validator;

    /* @formatter:off */
	@CrossOrigin /* to allow call from getsechub.detss and maybe other sites using javascript*/
	@UseCaseUserSignup(@Step(number=1, name="Rest API call",description="Rest api called to register user. Normally done by user itself",needsRestDoc=true))
	@Validated
	@RequestMapping(path = AdministrationAPIConstants.API_SIGNUP, method = RequestMethod.POST, produces= {MediaType.APPLICATION_JSON_VALUE})
	public void registerUser(@RequestBody @Valid SignupJsonInput signupInput) {
		/* @formatter:on */
        signupService.register(signupInput);
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(validator);
    }

}
