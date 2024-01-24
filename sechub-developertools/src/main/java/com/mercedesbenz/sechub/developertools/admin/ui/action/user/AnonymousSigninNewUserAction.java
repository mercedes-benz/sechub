// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class AnonymousSigninNewUserAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public AnonymousSigninNewUserAction(UIContext context) {
        super("Create new user sign in", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> name = getUserInput("Give name of new user", InputCacheIdentifier.USERNAME);
        if (!name.isPresent()) {
            return;
        }

        Optional<String> email = getUserInput("Give Email of new user", InputCacheIdentifier.EMAILADDRESS);
        if (!email.isPresent()) {
            return;
        }

        if (!confirm("Do you really want to sign up user? \n\nname: " + name.get() + "\nemail: " + email.get())) {
            return;
        }

        String userNameLowerCasedAndTimmed = name.get().toLowerCase().trim();
        String emailLowerCasedAndTrimmed = email.get().toLowerCase().trim();

        String infoMessage = getContext().getAdministration().createNewUserSignup(userNameLowerCasedAndTimmed, emailLowerCasedAndTrimmed);

        outputAsTextOnSuccess(infoMessage);
    }

}