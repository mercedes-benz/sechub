// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class ShowUserDetailForEmailAddressAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ShowUserDetailForEmailAddressAction(UIContext context) {
        super("Show user detail for email address", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> emailAddress = getUserInput("Please enter email address", InputCacheIdentifier.EMAILADDRESS);
        if (!emailAddress.isPresent()) {
            return;
        }

        String data = getContext().getAdministration().fetchUserInfoByEmailAddress(emailAddress.get());
        outputAsBeautifiedJSONOnSuccess(data);
    }

}