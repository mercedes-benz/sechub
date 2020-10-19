// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class DeleteProfileAction extends AbstractUIAction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(DeleteProfileAction.class);

    public DeleteProfileAction(UIContext context) {
        super("Delete execution profile", context);
    }

    @Override
    public void execute(ActionEvent e) {
        while(true) {
            ListExecutionProfilesDialogUI listProfilesDialog = new ListExecutionProfilesDialogUI(getContext(), "Select the profile you want to delete");
            listProfilesDialog.setOkButtonText("Delete profile");
            listProfilesDialog.showDialog();
            
            if (!listProfilesDialog.isOkPressed()) {
                return;
            }
            String profileId = listProfilesDialog.getSelectedValue();
            if (profileId == null) {
                /* ok pressed, but no selection */
                getContext().getOutputUI().output("No profile selected, so canceled");
                return;
            }
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

}