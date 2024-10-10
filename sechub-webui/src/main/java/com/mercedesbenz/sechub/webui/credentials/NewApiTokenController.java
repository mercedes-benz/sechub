// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.credentials;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webui.RequestConstants;
import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;
import com.mercedesbenz.sechub.webui.user.UserInfoService;

@Controller
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
