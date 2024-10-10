// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webserver.RequestConstants;

@Controller
public class LoginController {

    @GetMapping(RequestConstants.LOGIN)
    String login() {
        return "login";
    }
}