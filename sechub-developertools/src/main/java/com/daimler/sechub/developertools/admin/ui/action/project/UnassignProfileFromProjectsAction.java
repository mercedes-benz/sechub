// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.action.config.ListExecutionProfilesDialogUI;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.api.TestProject;

public class UnassignProfileFromProjectsAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public UnassignProfileFromProjectsAction(UIContext context) {
		super("Unassign profile from project(s)", context);
	}

	@Override
	public void execute(ActionEvent e) {
	    Optional<String> projectId = getUserInput("Please enter comma separated project IDs",InputCacheIdentifier.PROJECT_IDS);
        if (! projectId.isPresent()) {
            return;
        }
        String[] projectIds = projectId.get().split(",");
        for (String id: projectIds) {
            TestAPI.assertProject(new TestProject(id)).doesExist();
        }
        
        
        ListExecutionProfilesDialogUI dialogUI = new ListExecutionProfilesDialogUI(getContext(), "Select profile to assign to projects");
        dialogUI.setOkButtonText("Unassign from projects");
        dialogUI.showDialog();
        
        if(! dialogUI.isOkPressed()) {
            return;
        }
        String profileId = dialogUI.getSelectedValue();
        if (profileId==null) {
            return;
        }
		if (!confirm("Do you really want to unassign the profile " + profileId + " from the project IDs '" + projectId.get() + "' ?")) {
		    return;
		}

        getContext().getAdministration().removeProjectIdsFromProfile(profileId, projectIds);
		outputAsTextOnSuccess("Profile:"+profileId + " is now unassigned from projects:"+Arrays.asList(projectIds));
	}

}