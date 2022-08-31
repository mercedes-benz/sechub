// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.java.*;
import com.mercedesbenz.sechub.api.java.auth.Authentication;
import com.mercedesbenz.sechub.api.java.auth.HttpBasicAuth;
import com.mercedesbenz.sechub.api.java.demo.cli.exceptions.OpenAPITestToolRuntimeException;

public class SechubServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SechubServerTest.class);
    SecHubClient secHubClient = null;
    ApiClient apiClient = null;
    StringBuilder fullConnectionURI = new StringBuilder();

    public SechubServerTest(SecHubClient secHubClient) {
        this.secHubClient = secHubClient;
        fullConnectionURI.append(secHubClient.getHostUri()).append(":").append(secHubClient.getHostPort());
    }

    public void runAllTests() {
        if (!isServerAlive())
            throw new OpenAPITestToolRuntimeException("Sechub server is not alive.");

        if (!isListOfUsersFetched())
            throw new OpenAPITestToolRuntimeException("Can not fetch list of users from the Sechub server.");
    }

    private boolean isServerAlive() {
        LOG.info("*** Check on whether Sechub server is alive.");
        apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath(fullConnectionURI.toString());

        AnonymousApi apiInstance = new AnonymousApi(apiClient);
        try {
            apiInstance.anonymousCheckAliveGet();
            LOG.info("*** Sechub server is alive.");
            return true;
        } catch (Exception e) {
            LOG.error("*** Exception when calling AnonymousApi#anonymousCheckAliveGet:");
            e.printStackTrace();
        }
        return false;
    }

    private boolean isListOfUsersFetched() {
        LOG.info("*** Trying to fetch list of users.");

        HttpBasicAuth basicAuthentication = new HttpBasicAuth();
        basicAuthentication.setUsername(secHubClient.getUsername());
        basicAuthentication.setPassword(secHubClient.getSealedApiToken());
        Map<String, Authentication> authMap = new HashMap<>();
        authMap.put("basic", basicAuthentication);

        apiClient = new ApiClient(authMap);
        apiClient.setBasePath(fullConnectionURI.toString());

        AdminApi apiInstance = new AdminApi(apiClient);
        try {
            List<String> usersList = new ArrayList<>();
            usersList = apiInstance.adminListsAllUsers(basicAuthentication.toString());
            LOG.info("*** List of users: " + usersList);
            if (usersList.size() > 0) {
                LOG.info("*** List of users successfully fetched.");
                return true;
            }
        } catch (ApiException e) {
            LOG.error("*** Exception when calling AdminApi#adminListsAllUsers:");
            e.printStackTrace();
        }
        return false;
    }

}
