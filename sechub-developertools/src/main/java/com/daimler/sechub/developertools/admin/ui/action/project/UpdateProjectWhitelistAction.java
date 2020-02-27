// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class UpdateProjectWhitelistAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public UpdateProjectWhitelistAction(UIContext context) {
		super("Update project whitelist", context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> projectId = getUserInput("Please enter project ID/name",InputCacheIdentifier.PROJECT_ID);
		if (! projectId.isPresent()) {
			return;
		}

		List<String> data = getContext().getAdministration().fetchProjectWhiteList(projectId.get());
		List<String> result = getContext().getDialogUI().editList("Whitelist entries for "+projectId.get(), data);
		if (result==null) {
			return;
		}
		
		if (!confirm("Do you really want to update the project whitelist?")) {
		    return;
		}
		
		getContext().getAdministration().updateProjectWhiteList(projectId.get(),result);

	}

}