// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxContext;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;
import com.daimler.sechub.adapter.support.JSONAdapterSupport.Access;

// https://checkmarx.atlassian.net/wiki/spaces/KC/pages/202506366/Token-based+Authentication+v8.6.0+and+up
// having version 8.8.0 at installation we use the token base auth and no cookie approach
public class CheckmarxOAuthSupport {

	public void loginAndGetOAuthToken(CheckmarxContext context) throws AdapterException {
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
		map.add("client_secret", "014DF517-39D1-4453-B7B3-9930C563627C"); // client secret just ensures it is a checkmarx instance - so public...

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		// Endpoint example:
		// http://<server-name/ip>:<port>/cxrestapi/auth/identity/connect/token
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

		String json = response.getBody();
		CheckmarxOAuthData data = extractFromJson(context.json(), json);

		context.markAuthenticated(data);
	}

	CheckmarxOAuthData extractFromJson(JSONAdapterSupport support, String json)
			throws AdapterException {
		CheckmarxOAuthData data = new CheckmarxOAuthData();
		Access rootNode = support.fetchRootNode(json);
		data.accessToken=rootNode.fetch("access_token").asText();
		data.tokenType=rootNode.fetch("token_type").asText();
		data.expiresIn=rootNode.fetch("expires_in").asLong();

		return data;
	}

	public class CheckmarxOAuthData{
		private String accessToken;
		private long expiresIn;
		private String tokenType;
		public String getAccessToken() {
			return accessToken;
		}
		public long getExpiresIn() {
			return expiresIn;
		}
		public String getTokenType() {
			return tokenType;
		}
	}
}
