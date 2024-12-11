// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.asset;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.ManageAssetsDialogUI;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ManageAssetsAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ManageAssetsAction(UIContext context) {
        super("Manage assets", context);
    }

    @Override
    public void execute(ActionEvent e) {

        ManageAssetsDialogUI ui = new ManageAssetsDialogUI(getContext());
        ui.show();

    }

}