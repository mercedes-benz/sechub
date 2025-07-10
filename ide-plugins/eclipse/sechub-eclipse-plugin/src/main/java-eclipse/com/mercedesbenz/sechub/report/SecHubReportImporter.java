// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import static com.mercedesbenz.sechub.util.EclipseUtil.createErrorStatus;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.model.SecHubReportToFindingModelTransformer;

public class SecHubReportImporter {

	private SecHubReportToFindingModelTransformer transformer = new SecHubReportToFindingModelTransformer();
	private static final ObjectMapper mapper = new ObjectMapper();

	public void importAndDisplayReport(File reportFile) {
		importByJob(new SecHubReportFileImportJob(reportFile));
	}

	public void importAndDisplayReport(SecHubReport report, String projectId) {
		importByJob(new SecHubReportImportJob(report, projectId));
	}

	private void importByJob(Job job) {
		job.setUser(true);
		job.schedule();
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
			importReport(report, null, monitor);

			return Status.OK_STATUS;

		} catch (RuntimeException e) {
			return createErrorStatus("Unexpected error on import happened", e);
		} catch (IOException e) {
			return createErrorStatus("An error occured while reading the report: " + absolutePath
					+ ". Make sure the report is an actual SecHub Report.");
		}
	}

	private IStatus importAndDisplayReportInsideJob(SecHubReport report, String projectId, IProgressMonitor monitor) {
		monitor.beginTask("Import SecHub report data", 2);
		importReport(report, projectId, monitor);
		return Status.OK_STATUS;
	}

	/**
	 * Will do 2 works on progress monitor
	 */
	private void importReport(SecHubReport report, String projectId, IProgressMonitor monitor) {
		FindingModel model = transformer.transform(report, projectId);
		monitor.worked(1);

		SecHubReportViewUpdater.updateReportViewInSWTThread(report, model);

		monitor.worked(1);
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

	private class SecHubReportFileImportJob extends Job {

		private File reportFile;

		public SecHubReportFileImportJob(File reportFile) {
			super("Start import of report from file: " + reportFile);
			this.reportFile = reportFile;
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			return importAndDisplayReportInsideJob(reportFile, monitor);
		}

	}

	private class SecHubReportImportJob extends Job {

		private SecHubReport report;
		private String projectId;

		public SecHubReportImportJob(SecHubReport report, String projectId) {
			super("Start import of report by data");
			this.report = report;
			this.projectId = projectId; // currently there is no project information inside the report
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			return importAndDisplayReportInsideJob(report, projectId, monitor);
		}

	}

}
