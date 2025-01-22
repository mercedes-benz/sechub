package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserFetchesUserDetailInformation;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserUpdatesEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserVerifiesEmailAddress;

import jakarta.annotation.security.RolesAllowed;

@RestController
@RolesAllowed(RoleConstants.ROLE_USER)
public class UserRestController {

    private final UserDetailInformationService userDetailInformationService;

    private final UserEmailAddressUpdateService emailAddressUpdateService;

    UserRestController(UserDetailInformationService userDetailInformationService, UserEmailAddressUpdateService emailAddressUpdateService) {
        this.userDetailInformationService = userDetailInformationService;
        this.emailAddressUpdateService = emailAddressUpdateService;
    }

    /* @formatter:off */
    @UseCaseUserFetchesUserDetailInformation(@Step(number=1,name="Rest call",description="Json response containing details about the authenticated user",needsRestDoc=true))
    @GetMapping(AdministrationAPIConstants.API_USER_DETAIL_INFO)
    public UserDetailInformation fetchUserDetailInformation() {
        /* @formatter:on */
        return userDetailInformationService.fetchDetails();
    }

    /* @formatter:off */
    @UseCaseUserUpdatesEmailAddress(@Step(number = 1, name = "Rest call", description = "User request to update his email address", needsRestDoc = true))
    @PutMapping (AdministrationAPIConstants.API_USER_UPDATE_EMAIL)
    public void updateUserEmailAddress(@PathVariable(name="newEmailAddress") String emailAddress) {
        /* @formatter:on */
        emailAddressUpdateService.userUpdatesEmailAddress(emailAddress);
    }

    @UseCaseUserVerifiesEmailAddress(@Step(number = 1, name = "Rest call", description = "User verifies his new email address", needsRestDoc = true))
    @GetMapping(AdministrationAPIConstants.API_USER_VERIFY_EMAIL)
    public void verifyEmailAddress(@PathVariable(name = "token") String token) {
        emailAddressUpdateService.verifyUserEmailAddress(token);
    }

}
