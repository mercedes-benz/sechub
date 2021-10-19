// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

public class AssertSignup extends AbstractAssert{

	private TestUser user;

	AssertSignup(TestUser user) {
		this.user = user;
	}

	public AssertSignup doesNotExist() {
		assertFalse("DID found signup for user:"+user.getUserId(), contains(user));
		return this;
	}

	public AssertSignup doesExist() {
		assertTrue("Did NOT found signup for:"+user.getUserId(), contains(user));
		return this;
	}
	
	private boolean contains(TestUser user) {
		String details = fetchUserDetails();
		String toSearchFor = "\""+user.getUserId()+"\"";
		return details.contains(toSearchFor);	
	}
	
	private String fetchUserDetails() {
		return getRestHelper().getJSON(getUrlBuilder().buildAdminListsUserSignupsUrl());
	}

}
