// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;

import com.mercedesbenz.sechub.server.SecHubProjectSelectionStorage;
import com.mercedesbenz.sechub.server.SecHubServerView;
import com.mercedesbenz.sechub.util.EclipseUtil;

public class SecHubWorkbenchListener implements IWorkbenchListener {

	@Override
	public boolean preShutdown(IWorkbench workbench, boolean forced) {

		storeCurrentSelectedProject();

		return true;
	}

	@Override
	public void postShutdown(IWorkbench workbench) {
	}

	private void storeCurrentSelectedProject() {
		IWorkbenchPage page = EclipseUtil.getActivePage();
		if (page == null) {
			return;
		}
		IViewPart view = page.findView(SecHubServerView.ID);
		if (view instanceof SecHubServerView serverView) {
			String selectedProjectId = serverView.getSelectedProjectId();

			SecHubProjectSelectionStorage.saveAsSelectedProjectId(selectedProjectId);
		}
	}
}