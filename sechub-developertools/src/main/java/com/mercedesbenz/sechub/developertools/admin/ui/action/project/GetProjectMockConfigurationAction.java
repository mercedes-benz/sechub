// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.mercedesbenz.sechub.developertools.JSONDeveloperHelper;
import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class GetProjectMockConfigurationAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public GetProjectMockConfigurationAction(UIContext context) {
        super("Get project mock config", context);
    }

    @Override
    protected void execute(ActionEvent e) {
        Optional<String> projectId = getUserInput("Please enter projectId to fetch mock configuration", InputCacheIdentifier.PROJECT_ID);
        if (!projectId.isPresent()) {
            return;
        }
        DeveloperAdministration administration = getContext().getAdministration();
        String url = administration.getUrlBuilder().buildGetProjectMockConfiguration(asSecHubId(projectId.get()));
        String json = administration.getRestHelper().getJSON(url);
        getContext().getOutputUI().output(JSONDeveloperHelper.INSTANCE.beatuifyJSON(json));

    }

}