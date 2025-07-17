// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class SechubPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {

		IPreferenceStore store = SecHubPreferences.get().getScopedPreferenceStore();
		store.setDefault(PreferenceIdConstants.USE_CUSTOM_WEBUI_LOCATION, false);
		store.setDefault(PreferenceIdConstants.CUSTOM_WEBUI_LOCATION, "");
		store.setDefault(PreferenceIdConstants.TRUST_ALL, false);
	}
}