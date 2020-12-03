// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.ThreeButtonDialogResult;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class CreateProjectAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public CreateProjectAction(UIContext context) {
		super("Create project", context);
		setIcon(getClass().getResource("/icons/material-io/twotone_add_black_18dp.png"));
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> projectId = getUserInput("Please enter project ID/name", InputCacheIdentifier.PROJECT_ID);
		if (!projectId.isPresent()) {
			return;
		}
		
		// in case the user does not input any value, the value will be an empty string. 
		// Null will only be returned if the user pressed cancel.
		Optional<String> description = getUserInput("Please enter a short description (optional)");
		if (!description.isPresent()) {
			return;
		}

		Optional<String> owner = getUserInput("Please enter owner user id (must exist)", InputCacheIdentifier.USERNAME);
		if (!owner.isPresent()) {
			return;
		}

		List<String> whiteListURLs = new ArrayList<>();
		int i = 1;
		ThreeButtonDialogResult<String> uri;
		do {
			uri = getUserInputFromField("(Optional) whitelist uri[" + i + "]:");

			// stop if operation was canceled by the user
			if (uri.isCanceled()) {
				return;
			}

			if (uri.hasValue() && uri.isAdded() && !uri.getValue().trim().isEmpty()) {
				// only increase counter if value is not empty
				i++;
				whiteListURLs.add(uri.getValue());
			}
		} while (!uri.isFinished()); // continue until finished is pressed
		
		Map<String, String> metaData = new HashMap<>();
		ThreeButtonDialogResult<String> keyValue;
		do {
			keyValue = getUserInputFromField("(Optional) key=value pairs for metadata:");
			
			if(keyValue.isCanceled()) {
				return;
			}
			
			if (keyValue.hasValue() && keyValue.isAdded() && keyValue.getValue().contains("=")) {
				String[] elements = keyValue.getValue().split("=");
				String key = elements[0];
				String value = elements[1];
				metaData.put(key, value);
			}
		} while(!keyValue.isFinished());

		if (!confirm("Do you really want to create project ID/name " + projectId.get() + " with owner " + owner.get())) {
		    return;
		}
		
		// build and send request to server over HTTP
		String postResult = getContext().getAdministration().createProject(asSecHubId(projectId.get()), description.orElse(null),
				owner.get().toLowerCase().trim(), whiteListURLs, metaData);
		outputAsBeautifiedJSONOnSuccess(postResult);
		outputAsTextOnSuccess("project created:" + projectId);
	}

}