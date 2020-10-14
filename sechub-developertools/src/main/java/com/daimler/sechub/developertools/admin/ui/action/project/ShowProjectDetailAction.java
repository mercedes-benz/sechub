// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileList;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileListEntry;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class ShowProjectDetailAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public ShowProjectDetailAction(UIContext context) {
		super("Show project detail",context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> opt = getUserInput("Please enter project ID/name",InputCacheIdentifier.PROJECT_ID);
		if (! opt.isPresent()) {
			return;
		}
		String profileId = opt.get();
		String data = getContext().getAdministration().fetchProjectInfo(asSecHubId(opt.get()));
		outputAsBeautifiedJSONOnSuccess(data);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Profiles:\n");
		TestExecutionProfileList list = getContext().getAdministration().fetchExecutionProfileList();
		for (TestExecutionProfileListEntry entry: list.executionProfiles) {
		    TestExecutionProfile profile = getContext().getAdministration().fetchExecutionProfile(entry.id);
		    if (profile.projectIds.contains(profileId)) {
		        sb.append("- ");
		        sb.append(profile.id);
		        if (!profile.enabled) {
		            sb.append("(disabled)");
		        }
		        sb.append("\n  with executor configurations:");
		        
		        for (TestExecutorConfig config: profile.configurations) {
		            sb.append("\n   *").append(config.name).append(", enabled:").append(config.enabled).append(", uuid=").append(config.uuid);
		        }
		        sb.append("\n");
		    }
		}
		outputAsTextOnSuccess(sb.toString());
	}

}