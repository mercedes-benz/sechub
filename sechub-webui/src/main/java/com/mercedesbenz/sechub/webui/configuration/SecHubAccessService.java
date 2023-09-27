package com.mercedesbenz.sechub.webui.configuration;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.MockedSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;

@Service
public class SecHubAccessService {
    @Value("${sechub.server-url}")
    private String secHubServerUrl;

    @Value("${sechub.trust-all-certificates}")
    private boolean trustAllCertificates;
   
	private SecHubClient client;
	
    @PostConstruct
    void setupSecHubClient() {
		URI serverUri = URI.create(secHubServerUrl);
		
		this.client = MockedSecHubClient.from(serverUri, "mocked", "verySecretTrustMe", trustAllCertificates);
    }
    
	public SecHubClient getSecHubClient() {
		return this.client;
	}
	
	public URI getSecHubServerUri() {
		return this.client.getServerUri();
	}
}
