// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
	@Autowired
	SecHubServerAccessService accessService;

    @GetMapping("/")
    String index(Model model) {
        model.addAttribute("sechubServerUrl", accessService.getSecHubServerUrl());
        return "index";
    }
}
