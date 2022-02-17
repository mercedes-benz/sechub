// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class UnassignUserFromProjectAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public UnassignUserFromProjectAction(UIContext context) {
        super("Unassign user from project", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> userId = getUserInput("Please enter userId", InputCacheIdentifier.USERNAME);
        if (!userId.isPresent()) {
            return;
        }
        Optional<String> projectId = getUserInput("Please enter project ID/name", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }

        if (!confirm("Do you really want to unassign the userId " + userId.get() + " from the project ID/name " + projectId.get() + "?")) {
            return;
        }

        String infoMessage = getContext().getAdministration().unassignUserFromProject(asSecHubId(userId.get()), asSecHubId(projectId.get()));
        outputAsTextOnSuccess(infoMessage);
    }

}