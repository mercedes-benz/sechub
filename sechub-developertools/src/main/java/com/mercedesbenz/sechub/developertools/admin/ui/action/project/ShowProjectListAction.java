// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ShowProjectListAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ShowProjectListAction(UIContext context) {
        super("Show project list", context);
    }

    @Override
    public void execute(ActionEvent e) {
        String data = getContext().getAdministration().fetchProjectList();
        outputAsBeautifiedJSONOnSuccess(data);
    }

}