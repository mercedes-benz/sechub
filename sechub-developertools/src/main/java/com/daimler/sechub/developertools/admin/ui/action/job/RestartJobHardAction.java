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

public class RestartJobHardAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(RestartJobHardAction.class);

	public RestartJobHardAction(UIContext context) {
		super("Restart job (hard)", context);
	}

    @Override
    public void execute(ActionEvent e) {
        Optional<String> optJobUUID = getUserInput("Please enter job UUID to RESTART (hard)", InputCacheIdentifier.JOB_UUID);
        if (!optJobUUID.isPresent()) {
            return;
        }
        String jobUUID = optJobUUID.get();
        if (!confirm("Do you really want to\nRESTART (hard)\njob " + jobUUID + "?")) {
            outputAsTextOnSuccess("RESTART (hard) canceled");
            LOG.info("stopped restart of job {}", jobUUID);
            return;
        }
        outputAsTextOnSuccess("triggered restart of job:"+jobUUID);
        LOG.info("trigger restart of job {}", jobUUID);
        String infoMessage = getContext().getAdministration().restartJob(UUID.fromString(jobUUID));
        outputAsTextOnSuccess(infoMessage);
    }

}