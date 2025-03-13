// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.mercedesbenz.sechub.domain.administration.AdministrationAPIConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserFetchesUserDetailInformation;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseUserUpdatesEmailAddress;

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
    @UseCaseUserUpdatesEmailAddress(
            @Step(number = 1,
                    name = "Rest call",
                    description = "User request to update their email address",
                    next = {2},
                    needsRestDoc = true))
    @RequestMapping (value = AdministrationAPIConstants.API_USER_EMAIL+ "/{emailAddress}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserEmailAddress(@PathVariable(name="emailAddress") String newEmailAddress) {
        /* @formatter:on */
        emailAddressUpdateService.userRequestUpdateMailAddress(newEmailAddress);
    }
}
