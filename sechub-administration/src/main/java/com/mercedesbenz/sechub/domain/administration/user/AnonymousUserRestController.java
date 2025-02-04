package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAnonymousUserVerifiesEmailAddress;

@RestController
public class AnonymousUserRestController {

    private final UserEmailAddressUpdateService emailAddressUpdateService;

    AnonymousUserRestController(UserEmailAddressUpdateService emailAddressUpdateService) {
        this.emailAddressUpdateService = emailAddressUpdateService;
    }

    @UseCaseAnonymousUserVerifiesEmailAddress(@Step(number = 1, name = "Rest call", description = "User verifies his new email address", needsRestDoc = true))
    @GetMapping(AdministrationAPIConstants.API_ANONYMOUS_USER_VERIFY_EMAIL_BUILD + "/{oneTimeToken}")
    public void verifyEmailAddress(@PathVariable(name = "oneTimeToken") String oneTimeToken) {
        emailAddressUpdateService.userVerifiesUserEmailAddress(oneTimeToken);
    }
}
