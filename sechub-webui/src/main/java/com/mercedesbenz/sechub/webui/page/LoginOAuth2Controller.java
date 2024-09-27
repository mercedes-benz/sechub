// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webui.ApplicationProfiles;
import com.mercedesbenz.sechub.webui.RequestConstants;

@Controller
@Profile({ ApplicationProfiles.OAUTH2_ENABLED, ApplicationProfiles.TEST })
class LoginOAuth2Controller {

    @GetMapping(RequestConstants.LOGIN_OAUTH2)
    String login(Model model) {
        // TODO: make this configurable later for multiple client registrations
        String registrationId = "mercedes-benz";
        model.addAttribute("registrationId", registrationId);
        return "login-oauth2";
    }
}