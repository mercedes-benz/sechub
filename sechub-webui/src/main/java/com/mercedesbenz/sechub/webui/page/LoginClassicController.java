// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webui.RequestConstants;

@Controller
public class LoginClassicController {

    @GetMapping(RequestConstants.LOGIN_CLASSIC)
    String login() {
        return "login-classic";
    }
}