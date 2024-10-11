// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.page;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webserver.ApplicationProfiles;
import com.mercedesbenz.sechub.webserver.RequestConstants;
import com.mercedesbenz.sechub.webserver.security.OAuth2Properties;

@Controller
@Profile(ApplicationProfiles.OAUTH2_ENABLED)
class LoginOAuth2Controller {

    private final OAuth2Properties oAuth2Properties;

    LoginOAuth2Controller(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

    @GetMapping(RequestConstants.LOGIN_OAUTH2)
    String login(Model model) {
        String registrationId = oAuth2Properties.getProvider();
        model.addAttribute("registrationId", registrationId);
        return "login-oauth2";
    }
}