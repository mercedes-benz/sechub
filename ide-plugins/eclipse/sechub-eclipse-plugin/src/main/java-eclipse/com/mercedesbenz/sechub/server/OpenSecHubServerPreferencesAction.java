// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.mercedesbenz.sechub.EclipseUtil;
import com.mercedesbenz.sechub.preferences.SecHubPreferences;

public class OpenSecHubServerPreferencesAction extends Action {
	
	private static ImageDescriptor IMAGE_DESCRIPTOR = EclipseUtil.createImageDescriptor("/icons/edit-config.png");

	public OpenSecHubServerPreferencesAction() {
		 setImageDescriptor(IMAGE_DESCRIPTOR);
         setText("Open preferences");
         setToolTipText("Opens server preference page");
	}
	
	@Override
	public void run() {
		SecHubPreferences.get().openServerPreferences();
	}
}
