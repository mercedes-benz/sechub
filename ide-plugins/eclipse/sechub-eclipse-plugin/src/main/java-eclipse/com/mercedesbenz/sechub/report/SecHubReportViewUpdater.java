// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.util.EclipseUtil;
import com.mercedesbenz.sechub.util.Logging;

public class SecHubReportViewUpdater {

	public static void updateReportViewInSWTThread(SecHubReport report, FindingModel model) {
		EclipseUtil.safeAsyncExec(() -> internalUpdateReportView(report, model));
	}

	private static void internalUpdateReportView(SecHubReport report, FindingModel model) {
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

//		model.setJobUUID(report.getJobUUID());
//		model.setTrafficLight(report.getTrafficLight());
//		SecHubStatus status = report.getStatus();
//		model.setStatus(status == null ? null: status.getValue());

		reportView.update(model);
	}
}
