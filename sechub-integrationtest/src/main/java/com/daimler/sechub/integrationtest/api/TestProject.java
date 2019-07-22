// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.List;

public class TestProject {

	private String projectId;
	private String description;
	private List<String> whiteListUrls;

	private static final List<TestProject> all = new ArrayList<>();

	TestProject() {
		all.add(this);
	}

	public TestProject(String projectId, String ... whiteListUrls) {
		this.description="description of "+projectId;
		this.projectId=projectId;
		this.whiteListUrls=new ArrayList<>();
		for (String whitelist : whiteListUrls) {
			this.whiteListUrls.add(whitelist);
		}
	}

	public String getProjectId() {
		return projectId;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getWhiteListUrls() {
		return whiteListUrls;
	}
}
