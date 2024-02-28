// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page.credentials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webui.RequestConstants;
import com.mercedesbenz.sechub.webui.page.user.UserInfoService;
import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;

@Controller
public class NewApiTokenController {

    @Autowired
    NewApiTokenService newApiTokenService;

    @Autowired
    SecHubAccessService accessService;

    @Autowired
    UserInfoService userInfoService;

    @GetMapping(RequestConstants.REQUEST_NEW_APITOKEN)
    String requestNewApiToken(Model model) {
        String emailAddress = userInfoService.getEmailAddress();

        model.addAttribute("sechubServerUrl", accessService.getSecHubServerUri());
        model.addAttribute("successfullyRequestedToken", newApiTokenService.requestNewApiToken(emailAddress));
        model.addAttribute("userEmail", emailAddress);

        return "new-apitoken";
    }
}
