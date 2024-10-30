// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webserver.ApplicationProfiles;
import com.mercedesbenz.sechub.webserver.RequestConstants;
import com.mercedesbenz.sechub.webserver.security.OAuth2Properties;

@Controller
class LoginController {

    private final OAuth2Properties oAuth2Properties;
    private final boolean isOAuth2Enabled;
    private final boolean isClassicAuthEnabled;

    LoginController(@Autowired(required = false) OAuth2Properties oAuth2Properties, Environment environment) {
        this.oAuth2Properties = oAuth2Properties;
        this.isOAuth2Enabled = environment.matchesProfiles(ApplicationProfiles.OAUTH2_ENABLED);
        this.isClassicAuthEnabled = environment.matchesProfiles(ApplicationProfiles.CLASSIC_AUTH_ENABLED);
    }

    @GetMapping({ RequestConstants.ROOT, RequestConstants.LOGIN })
    String login(Model model) {
        model.addAttribute("isOAuth2Enabled", isOAuth2Enabled);
        model.addAttribute("isClassicAuthEnabled", isClassicAuthEnabled);

        if (oAuth2Properties != null) {
            String registrationId = oAuth2Properties.getProvider();
            model.addAttribute("registrationId", registrationId);
        }

        return "login";
    }

}