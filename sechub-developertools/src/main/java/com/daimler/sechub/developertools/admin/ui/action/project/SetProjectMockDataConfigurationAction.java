// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class SetProjectMockDataConfigurationAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public SetProjectMockDataConfigurationAction(UIContext context) {
		super("Set project mock config", context);
	}

	@Override
	protected void execute(ActionEvent e) throws Exception {
		Optional<String> projectId = getUserInput("Please enter projectId to setup mock configuration", InputCacheIdentifier.PROJECT_ID);
		if (!projectId.isPresent()) {
			return;
		}
		Optional<String> projectMockConfig = getUserInputFromTextArea("Please enter mock configuration for project:" + projectId.get(),
				InputCacheIdentifier.PROJECT_MOCK_CONFIG_JSON);
		if (!projectMockConfig.isPresent()) {
			return;
		}
		
		if (!confirm("Do you really want to change the mock configuration?")) {
		    return;
		}
		
		DeveloperAdministration administration = getContext().getAdministration();
		String url = administration.getUrlBuilder().buildSetProjectMockConfiguration(asSecHubId(projectId.get()));
		administration.getRestHelper().putJSON(url, projectMockConfig.get());

	}

}