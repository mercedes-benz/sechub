package com.mercedesbenz.sechub.report;

import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;

import com.mercedesbenz.sechub.access.SecHubAccess;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.server.SecHubServerContext;
import com.mercedesbenz.sechub.util.EclipseUtil;

public class MarkFalsePositivesAction extends Action {

	private SecHubReportView secHubReportView;

	public MarkFalsePositivesAction(SecHubReportView secHubReportView) {
		setText("Mark as false positive(s)");
		setToolTipText("Mark selected findings as false positives");
		setImageDescriptor(EclipseUtil.createImageDescriptor("/icons/false_positive_marked.png"));
		
		this.secHubReportView = secHubReportView;
	}

	@Override
	public void run() {
		if (secHubReportView == null) {
			return;
		}
		SecHubReport currentReport = secHubReportView.getCurrentReport();
		if (currentReport == null) {
			return;
		}
		UUID jobUUID = currentReport.getJobUUID();
		List<Integer> list = secHubReportView.fetchSelectedFindingIds();

		int amountOfFindings = list.size();
		if (amountOfFindings == 0) {
			MessageDialog.openInformation(EclipseUtil.getActiveWorkbenchShell(), "FP mark not possible",
					"You have not selected any job specific finding.");
			return;
		}
		SecHubAccess access = SecHubServerContext.INSTANCE.getAccessOrNull();
		if (access == null || !access.fetchServerAccessStatus().isAlive()) {
			ErrorDialog.openError(EclipseUtil.getActiveWorkbenchShell(), "FP mark not possble",
					"Unmark is not possible because currently no server access", Status.error("No connection"));
			return;
		}
		MarkJobFalsePositivesDialog dialog = new MarkJobFalsePositivesDialog(EclipseUtil.getActiveWorkbenchShell(),
				list.size());
		int result = dialog.open();
		if (result != Dialog.OK) {
			return;
		}

		String comment = dialog.getComment();
		String projectId = SecHubServerContext.INSTANCE.getSelectedProjectId();
		
		try {
			access.markJobFalsePositives(projectId, jobUUID, comment, list);
		} catch (ApiException e) {
			ErrorDialog.openError(EclipseUtil.getActiveWorkbenchShell(), "FP mark not possble",
					"Was not able to mark as false psoitive, because of communication error",
					Status.error("Failed", e));
			return;
		}
		SecHubServerContext.INSTANCE.reloadFalsePositiveDataForCurrentProject();

		secHubReportView.recalculateFalsePositives();

	}

}
