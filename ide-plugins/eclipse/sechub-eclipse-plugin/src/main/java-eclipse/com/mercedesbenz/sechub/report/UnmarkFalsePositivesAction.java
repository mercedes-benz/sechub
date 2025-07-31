package com.mercedesbenz.sechub.report;

import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;

import com.mercedesbenz.sechub.access.SecHubAccess;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.server.SecHubServerContext;
import com.mercedesbenz.sechub.util.EclipseUtil;

public class UnmarkFalsePositivesAction extends Action {

	private SecHubReportView secHubReportView;

	public UnmarkFalsePositivesAction(SecHubReportView secHubReportView) {
		this.secHubReportView = secHubReportView;
		
		setText("Unmark false positive(s)");
		setToolTipText("Unmark selected findings from being false positives");
		setImageDescriptor(EclipseUtil.createImageDescriptor("/icons/false_positive_unmark.png"));
		
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
			MessageDialog.openInformation(EclipseUtil.getActiveWorkbenchShell(), "FP unmark not possible",
					"You have not selected any job specific finding.");
			return;
		}
		SecHubAccess access = SecHubServerContext.INSTANCE.getAccessOrNull();
		if (access == null || !access.fetchServerAccessStatus().isAlive()) {
			ErrorDialog.openError(EclipseUtil.getActiveWorkbenchShell(), "FP unmark not possble",
					"It is not possible to unmark false psoitive, because currently no server access", Status.error("No connection"));
			return;
		}
		
		boolean confirmed = MessageDialog.openConfirm(EclipseUtil.getActiveWorkbenchShell(), "Confirm unmark",
				"Are you sure you want to unmark " + amountOfFindings + " false positive(s) ?");
		if (!confirmed) {
			return;
		}


		String projectId = SecHubServerContext.INSTANCE.getSelectedProjectId();
		try {
			access.unmarkJobFalsePositives(projectId, jobUUID, list);
		} catch (ApiException e) {
			ErrorDialog.openError(EclipseUtil.getActiveWorkbenchShell(), "FP unmark not possble",
					"Was not able to unmark false psoitive, because of communication error",
					Status.error("Failed", e));
			return;
		}
		SecHubServerContext.INSTANCE.reloadFalsePositiveDataForCurrentProject();

		secHubReportView.recalculateFalsePositives();

	}

}
