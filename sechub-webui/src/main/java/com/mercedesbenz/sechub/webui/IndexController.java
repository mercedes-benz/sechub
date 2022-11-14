// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @Value("${sechub.serverUrl}")
    private String secHubServerUrl;

    @GetMapping("/")
    String index(Model model) {
        model.addAttribute("sechubServerUrl", secHubServerUrl);
        return "index";
    }
}
