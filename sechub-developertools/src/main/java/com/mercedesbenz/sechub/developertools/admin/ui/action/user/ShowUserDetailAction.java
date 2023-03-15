// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class ShowUserDetailAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public ShowUserDetailAction(UIContext context) {
        super("Show user detail", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> userId = getUserInput("Please enter user id", InputCacheIdentifier.USERNAME);
        if (!userId.isPresent()) {
            return;
        }

        String data = getContext().getAdministration().fetchUserInfo(asSecHubId(userId.get()));
        outputAsBeautifiedJSONOnSuccess(data);
    }

}