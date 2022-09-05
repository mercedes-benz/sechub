package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatusController {
    @Autowired
    StatusService statusService;

    @Value("${sechub.serverUrl}")
    private String secHubServerUrl;

    @GetMapping("/status")
    String status(Model model) {
        String secHubServerVersion = statusService.getServerVersion();

        model.addAttribute("sechubServerUrl", secHubServerUrl);
        model.addAttribute("sechubServerVersion", secHubServerVersion);
        return "status";
    }
}
