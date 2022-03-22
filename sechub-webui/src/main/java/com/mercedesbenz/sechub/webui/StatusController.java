package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class StatusController {
	@Value("${sechub.serverUrl}")
	private String secHubServerUrl;
	
	@GetMapping("/status")
	String status(Model model) {	
		model.addAttribute("sechubServerUrl", secHubServerUrl);
		return "status";
	}
}
