// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.integrationtestserver;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.DeveloperAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class GetProjectMockConfigurationAction extends IntegrationTestAction {
	private static final long serialVersionUID = 1L;

	public GetProjectMockConfigurationAction(UIContext context) {
		super("Get project mock config", context);
	}


	@Override
	protected void executeImplAfterRestHelperSwitched(ActionEvent e) {
		Optional<String> projectId = getUserInput("Please enter projectId to fetch mock configuration",InputCacheIdentifier.PROJECT_ID);
		if (!projectId.isPresent()) {
			return;
		}
		DeveloperAdministration administration = getContext().getAdministration();
		String url = administration.getUrlBuilder().buildGetProjectMockConfiguration(projectId.get());
		String json = administration.getRestHelper().getJSon(url);
		getContext().getOutputUI().output(JSONDeveloperHelper.INSTANCE.beatuifyJSON(json));

	}

}