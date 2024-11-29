// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.spring.security.LoginClassicProperties;
import com.mercedesbenz.sechub.spring.security.LoginOAuth2Properties;

@Controller
class LoginController {

    private final LoginOAuth2Properties loginOAuth2Properties;
    private final boolean isOAuth2Enabled;
    private final boolean isClassicAuthEnabled;

    LoginController(@Autowired(required = false) LoginOAuth2Properties loginOAuth2Properties,
            @Autowired(required = false) LoginClassicProperties loginClassicProperties) {
        this.loginOAuth2Properties = loginOAuth2Properties;
        this.isOAuth2Enabled = loginOAuth2Properties != null;
        this.isClassicAuthEnabled = loginClassicProperties != null;
    }

    @GetMapping({ RequestConstants.ROOT, RequestConstants.LOGIN })
    String login(Model model) {
        model.addAttribute("isOAuth2Enabled", isOAuth2Enabled);
        model.addAttribute("isClassicAuthEnabled", isClassicAuthEnabled);

        if (loginOAuth2Properties != null) {
            String registrationId = loginOAuth2Properties.getProvider();
            model.addAttribute("registrationId", registrationId);
        }

        return "login";
    }

}