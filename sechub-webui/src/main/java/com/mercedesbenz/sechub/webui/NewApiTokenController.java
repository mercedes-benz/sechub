package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewApiTokenController {
	@Autowired
	NewApiTokenService service;
	
	@Value("${sechub.serverUrl}")
	private String secHubServerUrl;
	
	@GetMapping("/requestNewApiToken")
	String requestNewApiToken(Model model) {
		System.out.println(service.requestNewApiToken());
		model.addAttribute("sechubServerUrl", secHubServerUrl);
		return "newApiToken";
	}
}
