// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.encryption;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionStatus;

public class FetchSecHubEncryptionStatusAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public FetchSecHubEncryptionStatusAction(UIContext context) {
        super("Fetch SecHub encryption status", context);
    }

    @Override
    public void execute(ActionEvent e) {

        SecHubEncryptionStatus status = getContext().getAdministration().fetchEncryptionStatus();
        outputAsTextOnSuccess(status.toFormattedJSON());
    }

}