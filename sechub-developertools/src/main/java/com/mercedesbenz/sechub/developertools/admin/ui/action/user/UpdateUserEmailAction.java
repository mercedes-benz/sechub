// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class UpdateUserEmailAction extends AbstractUIAction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(UpdateUserEmailAction.class);

    public UpdateUserEmailAction(UIContext context) {
        super("Update user email address", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> optUserId = getUserInput("Please enter userid for user whose email address you want to change.", InputCacheIdentifier.USERNAME);
        if (!optUserId.isPresent()) {
            return;
        }
        String userId = optUserId.get().toLowerCase().trim();

        Optional<String> optNewEmailAddress = getUserInput("Please enter new email address for user " + userId);
        if (!optNewEmailAddress.isPresent()) {
            return;
        }
        String newEmailAddress = optNewEmailAddress.get().toLowerCase().trim();

        if (!confirm("Do you really want to\nCHANGE EMAIL ADDRESS of \nuser " + userId + " to '" + newEmailAddress + "'?")) {
            outputAsTextOnSuccess("CANCELED - delete");
            LOG.info("canceled email change of user {} to {}", userId, newEmailAddress);
            return;
        }
        LOG.info("start change user {} email to {}", userId, newEmailAddress);
        String infoMessage = getContext().getAdministration().changeUserEmailAddress(userId, newEmailAddress);
        outputAsTextOnSuccess(infoMessage);
    }

}