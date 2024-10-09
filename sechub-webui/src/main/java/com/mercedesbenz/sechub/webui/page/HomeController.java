// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webui.RequestConstants;

@Controller
class HomeController {

    @GetMapping({ RequestConstants.ROOT, RequestConstants.HOME })
    public String home(@AuthenticationPrincipal OidcUser principal, Model model) {
        if (principal != null) {
            model.addAttribute("principal", principal.getAttribute("name"));
        }
        return "home";
    }
}
