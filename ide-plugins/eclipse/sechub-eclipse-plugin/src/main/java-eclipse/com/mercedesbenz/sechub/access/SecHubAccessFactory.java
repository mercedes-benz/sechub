// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.access;

import org.eclipse.equinox.security.storage.StorageException;

import com.mercedesbenz.sechub.preferences.SecHubPreferences;

public class SecHubAccessFactory {

	public static SecHubAccess create() {

		SecHubPreferences preferences = SecHubPreferences.get();
		String serverURL = preferences.getServerURL();
		String username;
		String apiToken;

		try {
			username = preferences.getSecureStorageAccess().getUserId();
			apiToken = preferences.getSecureStorageAccess().getApiToken();

		} catch (StorageException e) {
			username = "";
			apiToken = "";
		}

		return new SecHubAccess(serverURL, username, apiToken, SecHubPreferences.get().getTrustAll());
	}
}
