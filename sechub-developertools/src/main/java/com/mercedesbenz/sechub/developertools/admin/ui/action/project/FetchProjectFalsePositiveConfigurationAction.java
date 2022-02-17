// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class FetchProjectFalsePositiveConfigurationAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public FetchProjectFalsePositiveConfigurationAction(UIContext context) {
        super("Fetch project false positive configuration", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> projectId = getUserInput("Please enter project ID/name", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }

        String data = getContext().getAdministration().fetchProjectFalsePositiveConfiguration(asSecHubId(projectId.get()));
        outputAsBeautifiedJSONOnSuccess(data);
    }

}