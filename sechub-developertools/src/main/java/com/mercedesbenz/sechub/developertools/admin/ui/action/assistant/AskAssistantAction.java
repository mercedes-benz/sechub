// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.assistant;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.UUID;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class AskAssistantAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public AskAssistantAction(UIContext context) {
        super("Ask assistant", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> projectId = getUserInput("Please enter projectId", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }
        Optional<String> jobUUIDAsString = getUserInput("Please enter jobUUID", InputCacheIdentifier.JOB_UUID);
        if (!jobUUIDAsString.isPresent()) {
            return;
        }
        UUID jobUUID = UUID.fromString(jobUUIDAsString.get().toLowerCase().trim());
        Optional<String> findingIdAsString = getUserInput("Please enter findingId");
        if (!findingIdAsString.isPresent()) {
            return;
        }
        int findingId = Integer.parseInt(findingIdAsString.get().toLowerCase().trim());

        String infoMessage = getContext().getAdministration().explainFinding(projectId.get().toLowerCase().trim(), jobUUID, findingId);
        outputAsTextOnSuccess(infoMessage);
    }

}