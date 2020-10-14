// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.util.List;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.action.config.ListExecutionProfilesDialogUI;

public class AssignProfileToAllProjectsAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public AssignProfileToAllProjectsAction(UIContext context) {
		super("Assign profile to all projects", context);
	}

	@Override
    public void execute(ActionEvent e) {
        List<String> list = getContext().getAdministration().fetchProjectIdList();
        ListExecutionProfilesDialogUI dialogUI = new ListExecutionProfilesDialogUI(getContext(), "Select profile to assign to all projects (" + list.size()+ ")");
        dialogUI.setOkButtonText("Assign to projects");
        dialogUI.showDialog();
        
        if(! dialogUI.isOkPressed()) {
            return;
        }
        String profileId = dialogUI.getSelectedValue();
        if (profileId==null) {
            return;
        }
        if (!confirm("Do you really want to assign the profile " + profileId + " to all projects (" + list.size()+ ") ?")) {
            return;
        }

        getContext().getAdministration().addProjectIdsToProfile(profileId, list);
        outputAsTextOnSuccess("Profile:"+profileId + " is now assigned to all projects");
    }

}