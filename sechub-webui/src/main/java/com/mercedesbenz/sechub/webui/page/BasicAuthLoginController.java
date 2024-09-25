// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webui.RequestConstants;

@Controller
public class BasicAuthLoginController {

    @GetMapping(RequestConstants.BASIC_AUTH_LOGIN)
    String login() {
        return "basic-auth-login";
    }
}