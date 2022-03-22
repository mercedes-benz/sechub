package com.mercedesbenz.sechub.webui.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {
	@Value("${sechub.serverUrl}")
	private String secHubServerUrl;
	
	@Bean
	public WebClient webClient() {
		return WebClient.builder().
				baseUrl(secHubServerUrl).
				build();
	}
}
