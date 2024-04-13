// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class AnonymousRequestNewAPITokenUserAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public AnonymousRequestNewAPITokenUserAction(UIContext context) {
        super("Request new API token for existing user", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> email = getUserInput("Email of user requesting new API token", InputCacheIdentifier.EMAILADDRESS);
        if (!email.isPresent()) {
            return;
        }

        if (!confirm("Do you really want to request a new API token for userid: " + email.get() + "?")) {
            return;
        }

        String infoMessage = getContext().getAdministration().requestNewApiToken(email.get().toLowerCase().trim());
        outputAsTextOnSuccess(infoMessage);
    }

}