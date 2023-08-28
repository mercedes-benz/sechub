// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.api.SecHubStatus;

@Service
public class StatusService {   
    @Autowired
    SecHubServerAccessService accessService;

    @Autowired
    CredentialService credentialService;
    
    private SecHubClient client = null;
    
    StatusService() {
    	client = new SecHubClient(URI.create("https://localhost:8443"), "int-test_superadmin", "int-test_superadmin-pwd", true);
    }

    // TODO: Use the generated SecHub API client
    // TODO: Figure out what credentials are used to communicate to SecHub and how
    // they get injected
    public SecHubStatus getSecHubServerStatusInformation() {
    	
    	SecHubStatus status = null;
		try {
			status = client.fetchSecHubStatus();
		} catch (SecHubClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return status;
    }
    
    public boolean isSecHubServerAlive() {
    	try {
			return client.isServerAlive();
		} catch (SecHubClientException e) {
			// TODO Auto-generated catch block
			return false;
		}
    }
    
    public Optional<String> getServerVersion() {
    	SecHubClient client = new SecHubClient(URI.create(accessService.getSecHubServerUrl()), "int-test_superadmin", "int-test_superadmin-pwd", true);
    	
    	try {
			return client.getServerVersion();
		} catch (SecHubClientException e) {
			// TODO Auto-generated catch block
			return Optional.empty();
		}
    	
    }
}
