// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class DeleteConfigurationAction extends AbstractUIAction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(DeleteConfigurationAction.class);

    public DeleteConfigurationAction(UIContext context) {
        super("Delete executor configuration", context);
    }

    @Override
    public void execute(ActionEvent e) {
        while(true) {
            ListExecutorConfigurationDialogUI dialogUI = new ListExecutorConfigurationDialogUI(getContext(), "Select configuration you want to delete");
            dialogUI.setOkButtonText("Delete configuration");
            dialogUI.showDialog();
            if (!dialogUI.isOkPressed()) {
                return;
            }
            UUID uuid = dialogUI.getSelectedValue();
            if (uuid==null) {
                return;
            }
            if (!confirm("Do you really want to\nDELETE\nconfig " + uuid + "?")) {
                outputAsTextOnSuccess("CANCELED - delete");
                LOG.info("canceled delete of config {}", uuid);
                return;
            }
            LOG.info("start delete of config {}", uuid);
            String infoMessage = getContext().getAdministration().deletExecutionConfig(uuid);
            outputAsTextOnSuccess(infoMessage);
        }
    }

}