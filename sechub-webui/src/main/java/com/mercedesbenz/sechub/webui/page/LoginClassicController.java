// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webui.ApplicationProfiles;
import com.mercedesbenz.sechub.webui.RequestConstants;

@Controller
@Profile(ApplicationProfiles.CLASSIC_AUTH_ENABLED)
class LoginClassicController {

    @GetMapping(RequestConstants.LOGIN_CLASSIC)
    String login() {
        return "login-classic";
    }
}