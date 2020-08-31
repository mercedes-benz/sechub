// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.UUID;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class UnmarkProjectFalsePositiveAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public UnmarkProjectFalsePositiveAction(UIContext context) {
        super("Unmark false positive", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> projectId = getUserInput("Please enter project ID/name", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }
        
        Optional<String> jobUUID = getUserInput("Please enter job UUID", InputCacheIdentifier.JOB_UUID);
        if (!jobUUID.isPresent()) {
            return;
        }
        
        Optional<String> findingId = getUserInput("Please enter findingId");
        if (!findingId.isPresent()) {
            return;
        }

        getContext().getAdministration().deleteFalsePositivesForProject(asSecHubId(projectId.get()),UUID.fromString(jobUUID.get()),Integer.valueOf(findingId.get()));

    }

}