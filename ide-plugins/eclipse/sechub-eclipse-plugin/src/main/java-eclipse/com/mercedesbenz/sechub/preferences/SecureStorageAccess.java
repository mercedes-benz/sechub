// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.preferences;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

public class SecureStorageAccess {
	
	private ISecurePreferences preferences;
	private ISecurePreferences node;
	
	private static final String SECURE_STORAGE_NODE = "sechubSecureStorage";
	
	public SecureStorageAccess() {
        preferences = SecurePreferencesFactory.getDefault();
        node = preferences.node(SECURE_STORAGE_NODE);
	}
	
	public ISecurePreferences getSecurePreferences() {
		return preferences;
	}

    public void storeSecureStorage(String username, String password) throws StorageException  {
        node.put(PreferenceIdConstants.USER_ID, username, true);
        node.put(PreferenceIdConstants.APITOKEN, password, true);
    }
    
    public String getUserId() throws StorageException {
    	String username = node.get(PreferenceIdConstants.USER_ID, "Sechub Username");
    	return username;
    }
    
    public String getApiToken() throws StorageException {
    	String apitoken = node.get(PreferenceIdConstants.APITOKEN, "Sechub ApiToken");
    	return apitoken;
    }
}
