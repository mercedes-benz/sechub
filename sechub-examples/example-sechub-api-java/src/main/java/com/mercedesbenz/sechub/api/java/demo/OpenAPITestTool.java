// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo;

import static com.mercedesbenz.sechub.api.java.demo.SimpleAssert.*;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.java.AdminApi;
import com.mercedesbenz.sechub.api.java.AnonymousApi;
import com.mercedesbenz.sechub.api.java.ApiException;
import com.mercedesbenz.sechub.api.java.SecHubAccess;
import com.mercedesbenz.sechub.api.java.demo.config.ConfigurationProvider;

public class OpenAPITestTool {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAPITestTool.class);

    public static void main(String[] args) {
        new OpenAPITestTool().start(args);
    }

    private void start(String[] args) {
        try {
            ConfigurationProvider configProvider = ConfigurationProvider.create(args);

            String userName = configProvider.getUser();
            String apiToken = configProvider.getApiToken();
            URI serverUri = configProvider.getServerUri();
            boolean trustAll = configProvider.isTrustAll();

            LOG.trace("Granted the following Sechub connection parameters:");
            LOG.trace("*** Sechub server URI: {}", serverUri);
            LOG.trace("*** Privileged user id: {}", userName);
            LOG.trace("*** Privileged user's API token: {}", "*".repeat(apiToken.length()));
            LOG.trace("*** trustAll: {}", trustAll);

            SecHubAccess sechubAccess = new SecHubAccess(serverUri, userName, apiToken, trustAll);

            testAnonymousApi(sechubAccess);
            testAdminApi(sechubAccess);

            LOG.info("Sechub server successfully tested.");
            System.out.println("[ OK ] SecHub was accessible with generated Java API");

        } catch (Exception e) {
            LOG.error("Sechub server testing failed: ", e);
        }

    }

    private void testAnonymousApi(SecHubAccess access) throws ApiException {
        logTitle("Start testing anonymous API");
        AnonymousApi anonymousApi = access.getAnonymousApi();

        anonymousApi.anonymousCheckAliveGet();
        logSuccess("Sechub server is alive (GET).");
        
        anonymousApi.anonymousCheckAliveHead();
        logSuccess("Sechub server is alive (HEAD).");

    }

    private void testAdminApi(SecHubAccess access) throws ApiException {
        logTitle("Start testing admin API");
        AdminApi adminApi = access.getAdminApi();

        List<String> usersList = adminApi.adminListsAllUsers();
        logSuccess("List of users has entries: " + usersList.size());

//        Object result = adminApi.adminChecksServerVersion();
//        assumeEquals("0.0.0", result, "Check server version");
        
//        String projectName = "pn"+System.currentTimeMillis();
//        
//        Project project = new Project();
//        project.setOwner(usersList.get(0));
//        project.setName(projectName);
//        project.setApiVersion("1.0");
//        
//        adminApi.adminCreatesProject(project);
//        logSuccess("Project "+projectName+" created");
//        
//        List<String> projects = adminApi.adminListsAllProjects();
//        assertEquals(true, projects.contains(projectName),"Project "+projectName+" was found in list");
//        
    }

    
    private void assumeEquals(Object obj1, Object obj2, String message) {
        assertEquals(obj1, obj2, message);
        /* if no errror... */
        logSuccess(message);
    }
    
    private void logTitle(String title) {
        LOG.info("");
        LOG.info("   {}  ", title);
        LOG.info("*".repeat(title.length() + 6));
    }
    
    private void logSuccess(String text) {
        LOG.info("  ✔️ {}", text);
    }

}
