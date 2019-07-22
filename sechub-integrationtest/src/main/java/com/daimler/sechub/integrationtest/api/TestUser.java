// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.List;

public class TestUser implements UserContext {

	private String userId;
	private String apiToken;
	private String email;
	
	private static final List<TestUser> all = new ArrayList<>();

	TestUser() {
		all.add(this);
	}

	public TestUser(String userid, String apiToken, String email) {
		this.userId=userid;
		this.apiToken=apiToken;
		this.email=email;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getApiToken() {
		return apiToken;
	}

	public boolean isAnonymous() {
		return userId==null||userId.isEmpty();
	}

	public void updateToken(String newToken) {
		if (isAnonymous()) {
			throw new IllegalStateException("anonymous users may not have token updated!");
		}
		this.apiToken=newToken;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return "TestUser [userId=" + userId + ", apiToken=" + apiToken + ", email=" + email + "]";
	}

}
