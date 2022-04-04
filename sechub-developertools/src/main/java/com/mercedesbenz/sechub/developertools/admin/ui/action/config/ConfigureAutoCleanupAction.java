// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ConfigureAutoCleanupAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ConfigureAutoCleanupAction(UIContext context) {
        super("Configure auto cleanup", context);
        setIcon(getClass().getResource("/icons/material-io/twotone_clean_hands_black_18dp.png"));
    }

    @Override
    public void execute(ActionEvent e) {
        String data = getContext().getAdministration().fetchAutoCleanupConfiguration();
        String result = getContext().getDialogUI().editString("Auto cleanup configuration", data);

        if (result == null) {
            return;
        }
        if (!confirm("Do you really want to change your auto cleanup configuration?")) {
            return;
        }
        String infoMessage = getContext().getAdministration().updateAutoCleanupConfiguration(result);
        outputAsTextOnSuccess(infoMessage);
    }

}