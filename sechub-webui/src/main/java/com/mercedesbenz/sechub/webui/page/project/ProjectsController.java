// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webui.RequestConstants;
import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;

@Controller
public class ProjectsController {

    @Autowired
    SecHubAccessService accessService;

    @Autowired
    ProjectInfoService projectInfoService;

    @GetMapping(value = { RequestConstants.ROOT, RequestConstants.PROJECTS })
    String index(Model model) {
        model.addAttribute("sechubServerUrl", accessService.getSecHubServerUri());
        model.addAttribute("projectIds", projectInfoService.fetchProjectIdsForCurrentUser());
        return "projects";
    }
}
