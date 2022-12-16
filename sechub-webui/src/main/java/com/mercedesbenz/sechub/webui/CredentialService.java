package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CredentialService {
    @Value("${sechub.userid}")
    private String userId;

	@Value("${sechub.apitoken}")
    private String apiToken;
	
    public String getUserId() {
		return userId;
	}

	public String getApiToken() {
		return apiToken;
	}
}
