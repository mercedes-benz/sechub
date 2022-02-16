// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ListSignupsAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ListSignupsAction(UIContext context) {
        super("List waiting user signups", context);
    }

    @Override
    public void execute(ActionEvent e) {
        String data = getContext().getAdministration().fetchSignups();
        outputAsBeautifiedJSONOnSuccess(data);
    }

}