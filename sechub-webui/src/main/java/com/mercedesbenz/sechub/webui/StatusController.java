// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.api.SecHubStatus;

@Controller
public class StatusController {
    @Autowired
    StatusService statusService;

    @Autowired
    SecHubServerAccessService accessService;

    @GetMapping("/status")
    String status(Model model) {
    	SecHubStatus secHubServerStatus = null;
		
    	secHubServerStatus = statusService.getSecHubServerStatusInformation();

		//model.addAttribute("secHubServerStatus", secHubServerStatus);
    	model.addAttribute("secHubServerAlive", statusService.isSecHubServerAlive());
        model.addAttribute("sechubServerUrl", accessService.getSecHubServerUrl());
        model.addAttribute("sechubServerVersion", statusService.getServerVersion());
        return "status";
    }
}