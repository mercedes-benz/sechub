// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class DeleteProfileAction extends AbstractUIAction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(DeleteProfileAction.class);

    public DeleteProfileAction(UIContext context) {
        super("Delete execution profile", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> opt = getUserInput("Please enter profileId for profile to DELETE", InputCacheIdentifier.EXECUTION_PROFILE_ID);
        if (!opt.isPresent()) {
            return;
        }
        String profileId=opt.get().trim();
        if (!confirm("Do you really want to\nDELETE\nprofile " + profileId + "?")) {
            outputAsTextOnSuccess("CANCELED - delete");
            LOG.info("canceled delete of profile {}", profileId);
            return;
        }
        LOG.info("start delete of profile {}", profileId);
        String infoMessage = getContext().getAdministration().deletExecutionProfile(profileId);
        outputAsTextOnSuccess(infoMessage);
    }

}