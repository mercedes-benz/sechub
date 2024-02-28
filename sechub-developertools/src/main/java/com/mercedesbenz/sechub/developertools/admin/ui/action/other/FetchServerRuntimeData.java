// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.other;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class FetchServerRuntimeData extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public FetchServerRuntimeData(UIContext context) {
        super("Fetch server runtime data", context);
    }

    @Override
    public void execute(ActionEvent e) {
        String infoMessage = getContext().getAdministration().checkVersion();
        outputAsTextOnSuccess(infoMessage);
    }

}