// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.page.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.webserver.RequestConstants;
import com.mercedesbenz.sechub.webserver.sechubaccess.SecHubAccessService;

@Controller
public class StatusController {

    @Autowired
    SecHubStatusService secHubStatusService;

    @Autowired
    SecHubAccessService accessService;

    @GetMapping(RequestConstants.STATUS)
    String status(Model model) {

        model.addAttribute("sechubStatus", secHubStatusService.getSecHubStatus());

        model.addAttribute("sechubServerAlive", accessService.isSecHubServerAlive());
        model.addAttribute("sechubServerUrl", accessService.getSecHubServerUri());
        model.addAttribute("sechubServerVersion", accessService.getServerVersion());

        return "status";
    }
}