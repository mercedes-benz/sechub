// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import java.util.UUID;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.mercedesbenz.sechub.EclipseUtil;
import com.mercedesbenz.sechub.Logging;
import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;
import com.mercedesbenz.sechub.model.FindingModel;

public class SecHubReportViewUpdater {

	public void updateReportViewInSWTThread(UUID jobUUID, TrafficLight trafficLight, FindingModel model) {
		EclipseUtil.safeAsyncExec(() -> internalUpdateReportView(jobUUID, trafficLight, model));
	}

	private void internalUpdateReportView(UUID jobUUID, TrafficLight trafficLight, FindingModel model) {
		IWorkbenchPage page = EclipseUtil.getActivePage();
		if (page == null) {
			throw new IllegalStateException("Workbench page not found");
		}
		IViewPart view = page.findView(SecHubReportView.ID);

		/* create and show the result view if it isn't created yet. */
		if (view == null) {
			try {
				view = page.showView(SecHubReportView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
			} catch (PartInitException e) {
				Logging.logError("Wasn't able create new SecHub report view", e);
				return;
			}

		}
		if (!(view instanceof SecHubReportView)) {
			throw new IllegalStateException("SecHub report view not found");
		}

		SecHubReportView reportView = (SecHubReportView) view;
		page.activate(reportView); // ensure report view is shown

		model.setJobUUID(jobUUID);
		model.setTrafficLight(trafficLight);

		reportView.update(model);
	}
}
