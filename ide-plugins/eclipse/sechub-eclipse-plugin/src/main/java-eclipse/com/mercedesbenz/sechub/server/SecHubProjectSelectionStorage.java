// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.mercedesbenz.sechub.util.Logging;

public class SecHubProjectSelectionStorage {

	private static final String FALLBACK_PROJECT_ID = "";
	private static final String NODE_ID = "com.mercedesbenz.sechub";
	private static final String SELECTED_SECHUB_SERVER_PROJECT_ID = "selectedSecHubServerProjectId";

	private SecHubProjectSelectionStorage() {

	}

	public static void saveAsSelectedProjectId(String projectId) {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(NODE_ID);
		if (preferences == null) {
			Logging.logError("No preferences found for node:" + NODE_ID);
			return;
		}
		if (projectId==null) {
			projectId=FALLBACK_PROJECT_ID; // value may not be null
		}
		preferences.put(SELECTED_SECHUB_SERVER_PROJECT_ID, projectId);
		try {
			preferences.flush();
		} catch (org.osgi.service.prefs.BackingStoreException e) {
			Logging.logError("Not able to store selected SecHub server project id in preferences", e);
		}
	}

	public static String loadSelectedProjectId() {

		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(NODE_ID);
		return preferences.get(SELECTED_SECHUB_SERVER_PROJECT_ID, FALLBACK_PROJECT_ID);

	}

}