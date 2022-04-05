// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class AcceptUserSignupAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public AcceptUserSignupAction(UIContext context) {
        super("Accept user sign up", context);
        setIcon(getClass().getResource("/icons/material-io/twotone_accessibility_new_black_18dp.png"));
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> userToSignup = getUserInput("Please enter userid of waiting user to accept", InputCacheIdentifier.USERNAME);
        if (!userToSignup.isPresent()) {
            return;
        }

        if (!confirm("Do you really want to accept the sign up request from: " + userToSignup.get() + "?")) {
            return;
        }

        String infoMessage = getContext().getAdministration().acceptSignup(userToSignup.get().toLowerCase().trim());
        outputAsTextOnSuccess(infoMessage);
    }

}