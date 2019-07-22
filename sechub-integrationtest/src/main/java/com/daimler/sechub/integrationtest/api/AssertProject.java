// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import org.springframework.http.HttpStatus;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;

public class AssertProject extends AbstractAssert {

	private TestProject project;

	AssertProject(TestProject project) {
		this.project = project;
	}

	public AssertProject doesNotExist() {
		expectHttpClientError(HttpStatus.NOT_FOUND, () -> fetchProjectDetails(), project.getProjectId() + " found!");
		return this;
	}

	public AssertProject doesExist() {
		fetchProjectDetails();// will fail with http error when not available
		return this;

	}

	private String fetchProjectDetails() {
		return getRestHelper().getJSon(getUrlBuilder().buildAdminGetProjectDetailsUrl(project.getProjectId()));
	}
	public AssertProject hasOwner(TestUser user) {
		return hasOwner(user,true);
	}
	private AssertProject hasOwner(TestUser user, boolean expected) {
		String content = fetchProjectDetails();
		String owner="<undefined>";
		try {
			owner = JSONAdapterSupport.FOR_UNKNOWN_ADAPTER.fetch("owner",content).asText();
		} catch (AdapterException e) {
			e.printStackTrace();
			fail("adapter json failure:"+e.getMessage());
		}
		if (expected && !user.getUserId().equals(owner)) {
			fail("User:" + user.getUserId() + " is NOT owner of project:" + project.getProjectId()+" but:"+owner);
		}else if (!expected && user.getUserId().equals(owner)) {
			fail("User:" + user.getUserId() + " is owner of project:" + project.getProjectId());
		}
		return this;
	}

	public AssertProject hasNotOwner(TestUser user) {
		return hasOwner(user,false);
	}

}
