// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ShowAdminListAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ShowAdminListAction(UIContext context) {
        super("Show admin list", context);
    }

    @Override
    public void execute(ActionEvent e) {
        String data = getContext().getAdministration().fetchAdminList();
        outputAsBeautifiedJSONOnSuccess(data);
    }

}