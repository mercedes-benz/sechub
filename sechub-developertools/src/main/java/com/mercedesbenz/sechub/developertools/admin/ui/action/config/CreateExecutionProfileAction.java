// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfile;

public class CreateExecutionProfileAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public CreateExecutionProfileAction(UIContext context) {
        super("Create execution profile", context);
    }

    @Override
    public void execute(ActionEvent e) {
        ExecutionProfileDialogUI dialogUI = new ExecutionProfileDialogUI(getContext(), "Create new execution profile");

        dialogUI.showDialog();

        if (!dialogUI.isOkPressed()) {
            return;
        }
        TestExecutionProfile profile = dialogUI.getUpdatedProfile();

        getContext().getAdministration().createExecutionProfile(profile);

        String data = getContext().getAdministration().fetchExecutionProfiles();
        outputAsBeautifiedJSONOnSuccess(data);
    }

}