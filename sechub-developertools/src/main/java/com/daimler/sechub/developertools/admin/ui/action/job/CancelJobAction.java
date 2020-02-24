// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.job;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class CancelJobAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(CancelJobAction.class);

	public CancelJobAction(UIContext context) {
		super("Cancel JOB", context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> optJobUUID = getUserInput("Please enter job UUID to CANCEL", InputCacheIdentifier.JOB_UUID);
		if (! optJobUUID.isPresent()) {
			return;
		}
		String jobUUID= optJobUUID.get();
		if (!confirm("Do you really want to\nCANCEL\njob "+jobUUID+"?")) {
			outputAsTextOnSuccess("CANCELED - delete");
			LOG.info("stopped cancelation of job {}",jobUUID);
			return;
		}
		LOG.info("start cancel of job {}",jobUUID);
		String infoMessage = getContext().getAdministration().cancelJob(UUID.fromString(jobUUID));
		outputAsTextOnSuccess(infoMessage);
	}

}