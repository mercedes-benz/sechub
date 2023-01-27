// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewApiTokenController {
    @Autowired
    NewApiTokenService newApiTokenService;

    @Autowired
    SecHubServerAccessService accessService;

    @GetMapping("/requestNewApiToken")
    String requestNewApiToken(Model model) {
        // TODO: Replace with real user email once proper authentication is added
        String email = "email@example.org";

        newApiTokenService.requestNewApiToken(email);
        model.addAttribute("sechubServerUrl", accessService.getSecHubServerUrl());
        model.addAttribute("userEmail", email);

        return "newApiToken";
    }
}
