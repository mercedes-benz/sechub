// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.mercedesbenz.sechub.EclipseUtil;

public class SecHubPreferences {
	
	private static final SecHubPreferences INSTANCE = new SecHubPreferences();
	private ScopedPreferenceStore scopedPreferenceStore;
	private SecureStorageAccess secureStorageAccess = new SecureStorageAccess();
	
	private SecHubPreferences() {
		this.scopedPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, SechubPreferencePage.PAGE_ID);
	}
	
	public static SecHubPreferences get() {
		return INSTANCE;
	}
	
	public void openServerPreferences() {
		openPreferencePage(SechubPreferencePage.PREFERENCE_PAGE_ID,true);
	}
	
	public void openPreferencePage(String id, boolean blockOnOpen) {
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(EclipseUtil.getActiveWorkbenchShell(), id, new String[] { id }, null);
		if (dialog==null) {
			return;
		}
		dialog.setBlockOnOpen(blockOnOpen);
		dialog.open();
		
	}

	public String getServerURL() {
        return scopedPreferenceStore.getString(PreferenceIdConstants.SERVER);
	}
	
	public SecureStorageAccess getSecureStorageAccess() {
		return secureStorageAccess;
	}

	public ScopedPreferenceStore getScopedPreferenceStore() {
		return scopedPreferenceStore;
	}
	
}
