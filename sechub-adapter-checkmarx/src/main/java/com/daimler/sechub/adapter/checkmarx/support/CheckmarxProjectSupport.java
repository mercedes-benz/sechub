// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxContext;
import com.daimler.sechub.adapter.checkmarx.CheckmarxSessionData;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;
import com.daimler.sechub.adapter.support.JSONAdapterSupport.Access;

public class CheckmarxProjectSupport {

	public void ensureProjectExists(CheckmarxContext context) throws AdapterException {
		CheckmarxAdapterConfig config = context.getConfig();
		String projectName = config.getProjectId();
		String teamId = config.getTeamIdForNewProjects();

		Map<String, String> map = new LinkedHashMap<>();
		map.put("projectName", projectName);
		map.put("teamId", teamId);
		String url = context.getAPIURL("projects", map);
		RestOperations restTemplate = context.getRestOperations();

		// https://checkmarx.atlassian.net/wiki/spaces/KC/pages/814285654/Swagger+Examples+v8.8.0+-+v2
		// https://checkmarx.atlassian.net/wiki/spaces/KC/pages/564330665/Get+All+Project+Details+-+GET+projects+v8.8.0+and+up
		// example:
		// CxRestAPI/projects?projectName=myProject&teamId=00000000-1111-1111-b111-989c9070eb11
		try {
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			context.setSessionData(extractFirstProjectFromJsonWithProjectArray(context.json(), response.getBody()));
			context.setNewProject(false);
			return;
		} catch (HttpStatusCodeException e) {
			if (e.getRawStatusCode() != 404) {
				/* only 404 - not found is accepted */
				throw context.asAdapterException("Unexpected HTTP status error", e);
			}
		}
		/* 404 error - okay, lets create */
		context.setSessionData(createProject(context));
		context.setNewProject(true);
	}

	private CheckmarxSessionData createProject(CheckmarxContext context) throws AdapterException {
		CheckmarxAdapterConfig config = context.getConfig();
		String projectName = config.getProjectId();
		String teamId = config.getTeamIdForNewProjects();

		Map<String, String> json = new TreeMap<>();
		json.put("name", projectName);
		json.put("owningTeam",teamId);
		json.put("isPublic","false");

		String url = context.getAPIURL("projects");
		String jsonAsString = context.json().toJSON(json);
		RestOperations restTemplate = context.getRestOperations();

		// https://checkmarx.atlassian.net/wiki/spaces/KC/pages/222265747/Create+Project+with+Default+Configuration+-+POST+projects
		// https://checkmarx.atlassian.net/wiki/spaces/KC/pages/814285654/Swagger+Examples+v8.8.0+-+v2
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.set("Content-Type", "application/json;v=2.0");

		HttpEntity<String> request = new HttpEntity<>(jsonAsString,headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
		return extractProjectFromJsonWithProjectCreationData(projectName, context.json(), response.getBody());
	}

	CheckmarxSessionData extractFirstProjectFromJsonWithProjectArray(JSONAdapterSupport support, String json)
			throws AdapterException {
		CheckmarxSessionData data = new CheckmarxSessionData();
		Access rootNode = support.fetchRootNode(json);
		Access first = support.fetchArray(0, rootNode.asArray());
		data.setProjectId(first.fetch("id").asLong());
		data.setProjectName(first.fetch("name").asText());
		return data;
	}

	CheckmarxSessionData extractProjectFromJsonWithProjectCreationData(String projectName,
			JSONAdapterSupport support, String json) throws AdapterException {
		CheckmarxSessionData data = new CheckmarxSessionData();
		Access rootNode = support.fetchRootNode(json);
		data.setProjectId(rootNode.fetch("id").asLong());
		data.setProjectName(projectName);
		return data;
	}
}
