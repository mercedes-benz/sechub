// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.other;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class CheckVersionAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public CheckVersionAction(UIContext context) {
        super("Check version", context);
    }

    @Override
    public void execute(ActionEvent e) {
        String infoMessage = getContext().getAdministration().checkVersion();
        outputAsTextOnSuccess(infoMessage);
    }

}