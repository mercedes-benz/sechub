// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webui.configuration.SecHubAccessService;

@Controller
public class IndexController {
    @Autowired
    SecHubAccessService accessService;

    @GetMapping("/")
    String index(Model model) {
        model.addAttribute("sechubServerUrl", accessService.getSecHubServerUri());
        return "index";
    }
}
