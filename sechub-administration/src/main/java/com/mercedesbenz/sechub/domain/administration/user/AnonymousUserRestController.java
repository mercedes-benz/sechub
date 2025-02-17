// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAnonymousUserVerifiesEmailAddress;

@RestController
public class AnonymousUserRestController {

    private final UserEmailAddressUpdateService emailAddressUpdateService;

    AnonymousUserRestController(UserEmailAddressUpdateService emailAddressUpdateService) {
        this.emailAddressUpdateService = emailAddressUpdateService;
    }

    /* @formatter:off*/
    @UseCaseAnonymousUserVerifiesEmailAddress(
            @Step(
                    number = 1,
                    name = "Rest call",
                    next = {2},
                    description = "User verifies his new email address",
                    needsRestDoc = true))
    /* @formatter:on*/
    @RequestMapping(value = AdministrationAPIConstants.API_ANONYMOUS_USER_VERIFY_EMAIL + "/{oneTimeToken}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmailAddress(@PathVariable(name = "oneTimeToken") String oneTimeToken) {
        emailAddressUpdateService.userVerifiesUserEmailAddress(oneTimeToken);
    }
}
