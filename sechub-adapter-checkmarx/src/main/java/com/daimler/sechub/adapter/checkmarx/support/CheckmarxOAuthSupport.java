// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterContext;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;
import com.daimler.sechub.adapter.support.JSONAdapterSupport.Access;

// https://checkmarx.atlassian.net/wiki/spaces/KC/pages/202506366/Token-based+Authentication+v8.6.0+and+up
// having version 8.8.0 at installation we use the token base auth and no cookie approach
// regarding https://checkmarx.atlassian.net/wiki/spaces/KC/pages/33980525/CxSAST+OData+API+Authentication+v9.0.0+and+up
// we handle the "expires_in" attribute
public class CheckmarxOAuthSupport {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxOAuthSupport.class);

    static final int MAXIMUM_MILLISECONDS_BEFORE_TOKEN_REFRESH = 5000;

    public void refreshBearerTokenWhenNecessary(CheckmarxAdapterContext context) throws AdapterException {

        if (isTokenRefreshNecessary(context)) {
            CheckmarxOAuthData data = context.getoAuthData();
            LOG.info("{} - OAuth token refresh is necessary, because token expires in {} milliseconds. Trigger token refresh.", context.getTraceID(),
                    data == null ? "<unknown>" : data.calculateMillisecondsBeforeTokenExpires());
            loginAndGetOAuthToken(context);
        }
    }

    public void loginAndGetOAuthToken(CheckmarxAdapterContext context) throws AdapterException {
        CheckmarxAdapterConfig config = context.getConfig();

        // example:
        // CxRestAPI/projects?projectId=myProject&teamId=00000000-1111-1111-b111-989c9070eb11

        String url = context.getAPIURL("auth/identity/connect/token");

        RestOperations restTemplate = context.getRestOperations();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", config.getUser());
        map.add("password", config.getPasswordOrAPIToken());
        map.add("grant_type", "password");
        map.add("scope", "sast_rest_api");
        map.add("client_id", "resource_owner_client");
        map.add("client_secret", config.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        // Endpoint example:
        // http://<server-name/ip>:<port>/cxrestapi/auth/identity/connect/token
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        String json = response.getBody();
        CheckmarxOAuthData data = extractFromJson(context.json(), json);

        context.markAuthenticated(data);
    }

    CheckmarxOAuthData extractFromJson(JSONAdapterSupport support, String json) throws AdapterException {
        CheckmarxOAuthData data = new CheckmarxOAuthData();

        Access rootNode = support.fetchRootNode(json);

        data.accessToken = rootNode.fetch("access_token").asText();
        data.tokenType = rootNode.fetch("token_type").asText();
        data.expiresInSeconds = rootNode.fetch("expires_in").asLong();

        return data;
    }

    boolean isTokenRefreshNecessary(CheckmarxAdapterContext context) {
        CheckmarxOAuthData oauthData = context.getoAuthData();
        boolean expired = false;

        if (oauthData == null) {
            expired = true;
        } else {
            long millisBeforeExpiration = oauthData.calculateMillisecondsBeforeTokenExpires();
            /* old data available - check if there is a need to refresh */
            expired = millisBeforeExpiration < MAXIMUM_MILLISECONDS_BEFORE_TOKEN_REFRESH;
        }
        return expired;
    }

}
