// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import static com.mercedesbenz.sechub.EclipseUtil.createErrorStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.model.SecHubFindingToFindingModelTransformer;

public class SecHubReportImporter {

	private SecHubFindingToFindingModelTransformer transformer = new SecHubFindingToFindingModelTransformer();
	private SecHubReportViewUpdater secHubReportViewUpdater;
	private static final ObjectMapper mapper = new ObjectMapper();

	public void importAndDisplayReport(File reportFile) {
		SecHubReportImporterJob job = new SecHubReportImporterJob(reportFile);
		job.setUser(true);
		job.schedule();
		secHubReportViewUpdater = new SecHubReportViewUpdater();
	}

	private IStatus importAndDisplayReportInsideJob(File reportFile, IProgressMonitor monitor) {
		if (reportFile == null) {
			return createErrorStatus("No report file available");
		}
		IStatus statusReadReportPossible = isReadReportProblemExistingAndHandled(reportFile);
		if (!statusReadReportPossible.isOK()) {
			return statusReadReportPossible;

		}
		String absolutePath = reportFile.getAbsolutePath();
		monitor.beginTask("Import SecHub report from " + absolutePath, 3);

		try {
			SecHubReport report = mapper.readValue(reportFile, SecHubReport.class);
			if (report == null) {
				return createErrorStatus("Reportfile importer returned null");
			}
			monitor.worked(1);
			List<SecHubFinding> secHubFindings = report.getResult().getFindings();

			FindingModel model = transformer.transform(secHubFindings);
			monitor.worked(1);

			secHubReportViewUpdater.updateReportViewInSWTThread(report.getJobUUID(), report.getTrafficLight(), model);

			monitor.worked(1);

			return Status.OK_STATUS;

		} catch (RuntimeException e) {
			return createErrorStatus("Unexpected error on import happened", e);
		} catch (IOException  e) {
			return createErrorStatus("An error occured while reading the report: " + absolutePath
					+ ". Make sure the report is an actual SecHub Report.");
		}
	}

	private IStatus isReadReportProblemExistingAndHandled(File reportFile) {

		if (!reportFile.exists()) {
			return createErrorStatus("Unable to find report: " + reportFile.getAbsolutePath());
		}
		if (reportFile.canRead()) {

			return Status.OK_STATUS;
		} else {
			return createErrorStatus("No permissions to read the report: " + reportFile.getAbsolutePath());
		}
	}

	private class SecHubReportImporterJob extends Job {

		private File reportFile;

		public SecHubReportImporterJob(File reportFile) {
			super("Start import of report " + reportFile);
			this.reportFile = reportFile;
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			return importAndDisplayReportInsideJob(reportFile, monitor);
		}

	}

}
