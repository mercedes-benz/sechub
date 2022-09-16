// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.config;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.pds.AbstractPDSAction;

public class ConfigurePDSAutoCleanupAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public ConfigurePDSAutoCleanupAction(UIContext context) {
        super("Configure PDS auto cleanup", context);
        setIcon(getClass().getResource("/icons/material-io/twotone_clean_hands_black_18dp.png"));
    }

    @Override
    protected void executePDS(PDSAdministration pds) {
        String data = pds.fetchPDSAutoCleanupConfiguration();
        String result = getContext().getDialogUI().editString("PDS Auto cleanup configuration", data);

        if (result == null) {
            return;
        }
        if (!confirm("Do you really want to change your PDS auto cleanup configuration?")) {
            return;
        }
        String infoMessage = pds.updatePDSAutoCleanupConfiguration(result);
        outputAsTextOnSuccess(infoMessage);
    }

}