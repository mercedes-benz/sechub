// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.util.EclipseUtil;

public class ReportInfoAction extends Action {

	private SecHubReportView view;

	private static ImageDescriptor IMAGE_DESCRIPTOR = EclipseUtil.createImageDescriptor("/icons/report-info.png");

	public ReportInfoAction(SecHubReportView view) {
		this.view = view;

		setImageDescriptor(IMAGE_DESCRIPTOR);
		setText("Report information");
		setToolTipText("Shows report information details");
	}

	@Override
	public void run() {
		FindingModel model = view.getModel();
		if (model==null) {
			return;
		}
		SecHubReport report = model.getReport();
		if (report==null) {
			return;
		}

		ReportInfoDialog dialog = new ReportInfoDialog(EclipseUtil.getActiveWorkbenchShell(), report);
		dialog.open();
		
	}
	
}
