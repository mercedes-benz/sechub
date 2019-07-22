// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

public interface UserContext {

	String getUserId();

	String getApiToken();

	boolean isAnonymous();

	void updateToken(String newToken);

	String getEmail();

}