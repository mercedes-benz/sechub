// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class DeleteUserAction extends AbstractUIAction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUserAction.class);

    public DeleteUserAction(UIContext context) {
        super("Delete user", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> optUserId = getUserInput("Please enter userid  for user to DELETE", InputCacheIdentifier.USERNAME);
        if (!optUserId.isPresent()) {
            return;
        }
        String userId = optUserId.get().toLowerCase().trim();
        if (!confirm("Do you really want to\nDELETE\nuser " + userId + "?")) {
            outputAsTextOnSuccess("CANCELED - delete");
            LOG.info("canceled delete of user {}", userId);
            return;
        }
        LOG.info("start delete of user {}", userId);
        String infoMessage = getContext().getAdministration().deleteUser(userId);
        outputAsTextOnSuccess(infoMessage);
    }

}