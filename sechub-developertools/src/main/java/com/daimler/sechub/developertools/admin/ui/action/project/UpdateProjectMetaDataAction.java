// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class UpdateProjectMetaDataAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public UpdateProjectMetaDataAction(UIContext context) {
		super("Update project metadata", context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> optProjectId = getUserInput("Please enter project ID/name",InputCacheIdentifier.PROJECT_ID);
		if (! optProjectId.isPresent()) {
			return;
		}

		String projectId = optProjectId.get().toLowerCase().trim();
		String data = getContext().getAdministration().fetchProjectMetaData(projectId);
		String result = getContext().getDialogUI().editString("MetaData entries for " + projectId, data);
		
		if (result==null) {
			return;
		}
		
		if (!confirm("Do you really want to update the project metadata for project:" + projectId + " ?")) {
		    return;
		}
		
		getContext().getAdministration().updateProjectMetaData(projectId, result);

	}

}