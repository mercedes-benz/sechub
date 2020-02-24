// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.job;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.UUID;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class DownloadFullscanDataForJobAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public DownloadFullscanDataForJobAction(UIContext context) {
		super("Download job scan data as zip", context);
	}

	@Override
	public void execute(ActionEvent e) {
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
		String infoText = getContext().getAdministration().triggerDownloadFullScan(sechubJobUUID);
		outputAsTextOnSuccess(infoText);
	}

}