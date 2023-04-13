package com.mercedesbenz.sechub.systemtest.connect;

import java.net.URI;

import com.mercedesbenz.sechub.api.java.SecHubClient;

public class SecHubServerConnector {

    private SecHubClient sechubClient;

    public void SecHubServerConnector(String serverBase, String userId, String apiToken) {
        URI uri = URI.create(serverBase);
        sechubClient = SecHubClient.create(userId, apiToken, uri);

    }

//
//    public SecHubClient createClient(SystemTestRuntimeContext context) {
//
//        return null;
//    }

}
