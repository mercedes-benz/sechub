package com.mercedesbenz.sechub.domain.administration.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    UserRestController(UserDetailInformationService userDetailInformationService) {
        this.userDetailInformationService = userDetailInformationService;
    }

    /* @formatter:off */
    @UseCaseUserFetchesUserDetailInformation(@Step(number=1,name="Rest call",description="Json response containing details about the authenticated user",needsRestDoc=true))
    @GetMapping(AdministrationAPIConstants.API_USER_DETAIL_INFO)
    public UserDetailInformation fetchUserDetailInformation() {
        /* @formatter:on */
        return userDetailInformationService.fetchDetails();
    }

    @UseCaseUserUpdatesEmailAddress(@Step(number = 1, name = "Rest call", description = "User updates his email address", needsRestDoc = true))
    @GetMapping(AdministrationAPIConstants.API_USER_UPDATE_EMAIL)
    public void updateUserEmailAddress() {
        // todo: implement

    }
}
