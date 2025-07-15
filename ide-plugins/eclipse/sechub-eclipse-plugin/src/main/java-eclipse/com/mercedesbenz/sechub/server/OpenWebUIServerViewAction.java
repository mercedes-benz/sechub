// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.mercedesbenz.sechub.preferences.SecHubPreferences;
import com.mercedesbenz.sechub.util.BrowserUtil;
import com.mercedesbenz.sechub.util.EclipseUtil;

public class OpenWebUIServerViewAction extends Action {

	private static ImageDescriptor IMAGE_DESCRIPTOR = EclipseUtil.createImageDescriptor("/icons/open-webui.png");
	private SecHubServerView view;

	public OpenWebUIServerViewAction(SecHubServerView view) {
		this.view = view;
		setImageDescriptor(IMAGE_DESCRIPTOR);
		setText("Open WebUI");
		setToolTipText("Open web ui in browser");
	}

	@Override
	public void run() {
		SecHubPreferences preferences = SecHubPreferences.get();
		String url = null;
		if (preferences.isUsingCustomWebUIUrl()) {
			url = preferences.getCustomWebUIUrl();
		} else {
			url = preferences.getServerURL() + "/login";
		}
		/*
		 * if not login configured, we can directly open project page instead - more
		 * convenient
		 */
		if (!url.endsWith("login")) {
			String projectId = view.getSelectedProjectId();
			if (projectId != null && !projectId.isBlank()) {
				if (!url.endsWith("/")) {
					url += "/";
				}
				url += "projects/" + projectId;
			}
		}

		BrowserUtil.openInExternalBrowser(url);

	}

}
