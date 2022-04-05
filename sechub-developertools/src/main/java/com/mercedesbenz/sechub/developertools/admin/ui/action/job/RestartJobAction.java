// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.job;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class RestartJobAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(RestartJobAction.class);

    public RestartJobAction(UIContext context) {
        super("Restart job", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> optJobUUID = getUserInput("Please enter job UUID to RESTART", InputCacheIdentifier.JOB_UUID);
        if (!optJobUUID.isPresent()) {
            return;
        }
        String jobUUID = optJobUUID.get();
        if (!confirm("Do you really want to\nRESTART\njob " + jobUUID + "?")) {
            outputAsTextOnSuccess("RESTART canceled");
            LOG.info("stopped restart of job {}", jobUUID);
            return;
        }
        outputAsTextOnSuccess("triggered restart of job:" + jobUUID);
        LOG.info("trigger restart of job {}", jobUUID);
        String infoMessage = getContext().getAdministration().restartJob(UUID.fromString(jobUUID));
        outputAsTextOnSuccess(infoMessage);
    }

}