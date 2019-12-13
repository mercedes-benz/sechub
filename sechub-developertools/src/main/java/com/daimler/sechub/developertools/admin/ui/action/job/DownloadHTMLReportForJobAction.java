// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.job;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.UUID;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class DownloadHTMLReportForJobAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public DownloadHTMLReportForJobAction(UIContext context) {
		super("Download HTML report", context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> projectId = getUserInput("Please enter project id",InputCacheIdentifier.PROJECT_ID);
		if (! projectId.isPresent()) {
			return;
		}
		Optional<String> jobUUID = getUserInput("Please enter job uuid", InputCacheIdentifier.JOB_UUID);
		if (!jobUUID.isPresent()) {
			return;
		}
		UUID sechubJobUUID = null;
		try {
			sechubJobUUID = UUID.fromString(jobUUID.get());
		} catch (Exception ex) {
			getContext().getOutputUI().error("Not a UUID:" + jobUUID.get(), ex);
			return;
		}
		String infoMessage = getContext().getAdministration().triggerDownloadReport(projectId.get(), sechubJobUUID);
		outputAsText(infoMessage);
	}

}