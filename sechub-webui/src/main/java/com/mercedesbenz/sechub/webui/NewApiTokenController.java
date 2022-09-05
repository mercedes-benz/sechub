package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewApiTokenController {
    @Autowired
    NewApiTokenService newApiTokenService;

    @Value("${sechub.serverUrl}")
    private String secHubServerUrl;

    @GetMapping("/requestNewApiToken")
    String requestNewApiToken(Model model) {
        // TODO: Replace with real user email once proper authentication is added
        String email = "email@example.org";

        newApiTokenService.requestNewApiToken(email);
        model.addAttribute("sechubServerUrl", secHubServerUrl);
        model.addAttribute("userEmail", email);

        return "newApiToken";
    }
}
