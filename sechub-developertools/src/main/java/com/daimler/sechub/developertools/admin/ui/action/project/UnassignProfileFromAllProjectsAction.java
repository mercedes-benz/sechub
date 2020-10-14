// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.List;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.action.config.ListExecutionProfilesDialogUI;

public class UnassignProfileFromAllProjectsAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public UnassignProfileFromAllProjectsAction(UIContext context) {
		super("Unassign profile from all projects", context);
	}

	@Override
    public void execute(ActionEvent e) {
        List<String> list = getContext().getAdministration().fetchProjectIdList();
        ListExecutionProfilesDialogUI dialogUI = new ListExecutionProfilesDialogUI(getContext(), "Select profile to unassign from all projects (" + list.size()+ ")");
        dialogUI.setOkButtonText("Unassign from allprojects");
        dialogUI.showDialog();
        
        if(! dialogUI.isOkPressed()) {
            return;
        }
        String profileId = dialogUI.getSelectedValue();
        if (profileId==null) {
            return;
        }
        if (!confirm("Do you really want to unassign the profile " + profileId + " from all projects (" + list.size()+ ") ?")) {
            return;
        }

        getContext().getAdministration().removeProjectIdsFromProfile(profileId, list);
        outputAsTextOnSuccess("Profile:"+profileId + " is now unassigned to all projects");
    }

}