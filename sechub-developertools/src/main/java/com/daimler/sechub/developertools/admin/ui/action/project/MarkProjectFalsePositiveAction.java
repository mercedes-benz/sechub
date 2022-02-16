// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class MarkProjectFalsePositiveAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public MarkProjectFalsePositiveAction(UIContext context) {
        super("Mark false positives", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> projectId = getUserInput("Please enter project ID/name", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }

        Optional<String> jsonOpt = getUserInputFromTextArea("Define false positives by json", InputCacheIdentifier.MARK_PROJECT_FALSE_POSITIVE);
        if (!jsonOpt.isPresent()) {
            return;
        }

        String data = getContext().getAdministration().markFalsePositivesForProjectByJobData(asSecHubId(projectId.get()), jsonOpt.get());
        outputAsBeautifiedJSONOnSuccess(data);

    }

}