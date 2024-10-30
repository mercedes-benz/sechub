// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.credentials;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webserver.ApplicationProfiles;
import com.mercedesbenz.sechub.webserver.RequestConstants;
import com.mercedesbenz.sechub.webserver.sechubaccess.SecHubAccessService;
import com.mercedesbenz.sechub.webserver.user.UserInfoService;

@Controller
@Profile(ApplicationProfiles.CLASSIC_AUTH_ENABLED)
class NewApiTokenController {

    private final NewApiTokenService newApiTokenService;
    private final SecHubAccessService accessService;
    private final UserInfoService userInfoService;

    NewApiTokenController(NewApiTokenService newApiTokenService, SecHubAccessService accessService, UserInfoService userInfoService) {
        this.newApiTokenService = newApiTokenService;
        this.accessService = accessService;
        this.userInfoService = userInfoService;
    }

    @GetMapping(RequestConstants.REQUEST_NEW_APITOKEN)
    String requestNewApiToken(Model model) {
        String emailAddress = userInfoService.getEmailAddress();

        model.addAttribute("sechubServerUrl", accessService.getSecHubServerUri());
        model.addAttribute("successfullyRequestedToken", newApiTokenService.requestNewApiToken(emailAddress));
        model.addAttribute("userEmail", emailAddress);

        return "new-apitoken";
    }
}
