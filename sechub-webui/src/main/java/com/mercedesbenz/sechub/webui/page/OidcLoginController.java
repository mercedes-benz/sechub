// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import com.mercedesbenz.sechub.webui.RequestConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OidcLoginController {

    @GetMapping(RequestConstants.OIDC_LOGIN)
    String login() {
        return "login";
    }
}