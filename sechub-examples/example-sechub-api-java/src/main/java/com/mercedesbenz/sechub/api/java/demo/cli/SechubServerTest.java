// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.cli;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercedesbenz.sechub.api.java.AdminApi;
import com.mercedesbenz.sechub.api.java.AnonymousApi;
import com.mercedesbenz.sechub.api.java.ApiClient;
import com.mercedesbenz.sechub.api.java.ApiException;
import com.mercedesbenz.sechub.api.java.Configuration;
import com.mercedesbenz.sechub.api.java.SecHubClient;
import com.mercedesbenz.sechub.api.java.demo.cli.exceptions.OpenAPITestToolRuntimeException;

public class SechubServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SechubServerTest.class);
    SecHubClient secHubClient = null;
    ApiClient apiClient = null;

    public SechubServerTest(SecHubClient secHubClient) {
        this.secHubClient = secHubClient;
    }

    void runAllTests() {
        if (!isServerAlive())
            throw new OpenAPITestToolRuntimeException("Sechub server is not alive.");

        if (!isListOfUsersFetched())
            throw new OpenAPITestToolRuntimeException("Can not fetch list of users from the Sechub server.");
    }

    private boolean isServerAlive() {
        LOG.info("*** Check on whether Sechub server is alive.");
        apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath(secHubClient.getHostUri().toString());

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

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        mapper.registerModule(new JavaTimeModule());
//        mapper.registerModule(new JsonNullableModule());
        
        HttpClient.Builder builder =  HttpClient.newBuilder();
       
        apiClient = new ApiClient(builder, mapper,secHubClient.getHostUri().toString());

        AdminApi apiInstance = new AdminApi(apiClient);
        try {
            List<String> usersList = new ArrayList<>();
            usersList = apiInstance.adminListsAllUsers();
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
