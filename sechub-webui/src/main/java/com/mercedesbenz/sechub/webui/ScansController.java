// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScansController {
	@Autowired
	SecHubServerAccessService accessSerivce;

    @GetMapping("/projects/{projectId}/scans")
    String scans(Model model) {
        model.addAttribute("sechubServerUrl", accessSerivce.getSecHubServerUrl());
        return "scans";
    }
}
