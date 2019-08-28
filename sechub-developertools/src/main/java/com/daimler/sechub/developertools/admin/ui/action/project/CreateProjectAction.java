// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class CreateProjectAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public CreateProjectAction(UIContext context) {
		super("Create project", context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> projectId = getUserInput("Please enter project ID/name",InputCacheIdentifier.PROJECT_ID);
		if (! projectId.isPresent()) {
			return;
		}
		Optional<String> description = getUserInput("Please enter a short description (optional)",null);
		Optional<String> owner = getUserInput("Please enter owner user id (must exist)",InputCacheIdentifier.USERNAME);
		if (! owner.isPresent()) {
			return;
		}

		List<String> whiteListURLs = new ArrayList<>();
		int i=1;
		Optional<String>  uri;
		do {
			uri = getUserInput("(Optional) whitelist uri["+i+"]:",InputCacheIdentifier.WHITELIST_URI);
			i++;
			if (uri.isPresent()) {
				whiteListURLs.add(uri.get());
			}
		}while(uri.isPresent());


		String data = getContext().getAdministration().createProject(projectId.get(),description.orElse(null), owner.get(),whiteListURLs);
		output(data);
	}

}