// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ListExecutionProfilesAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ListExecutionProfilesAction(UIContext context) {
        super("List execution profiles", context);
    }

    @Override
    public void execute(ActionEvent e) {
        String data = getContext().getAdministration().fetchExecutionProfiles();
        outputAsBeautifiedJSONOnSuccess(data);
    }

}