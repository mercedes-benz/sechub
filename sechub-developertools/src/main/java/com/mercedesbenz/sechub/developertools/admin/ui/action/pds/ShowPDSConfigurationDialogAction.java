// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ShowPDSConfigurationDialogAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ShowPDSConfigurationDialogAction(UIContext context) {
        super("Configure PDS access data", context);
    }

    @Override
    public final void execute(ActionEvent e) {
        JFrame mainFrame = getContext().getFrame();
        getContext().getPDSConfigurationUI().showInside(mainFrame);
    }
}
