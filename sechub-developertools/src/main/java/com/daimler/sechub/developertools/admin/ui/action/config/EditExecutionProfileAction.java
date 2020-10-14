// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.developertools.admin.DeveloperAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;

public class EditExecutionProfileAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public EditExecutionProfileAction(UIContext context) {
        super("Edit execution profile", context);
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/icons/material-io/twotone_edit_road_black_18dp.png")));
    }

    @Override
    public void execute(ActionEvent e) {
        while(true) {
            ListExecutionProfilesDialogUI listProfilesDialog = new ListExecutionProfilesDialogUI(getContext(), "Select the profile you want to edit");
            listProfilesDialog.setOkButtonText("Edit profile");
            listProfilesDialog.showDialog();
            
            if (!listProfilesDialog.isOkPressed()) {
                return;
            }
            String profileId = listProfilesDialog.getSelectedValue();
            if (profileId == null) {
                /* ok pressed, but no selection */
                getContext().getOutputUI().output("No profile selected, so canceled");
                return;
            }
            
            /* --------------------- */
            /* Edit selected profile: */
            /* --------------------- */
            DeveloperAdministration administration = getContext().getAdministration();
            TestExecutionProfile profile = administration.fetchExecutionProfile(profileId);
            
            /* dump to output */
            outputAsTextOnSuccess("Profile:" + profileId + " ass JSON:\n" + JSONConverter.get().toJSON(profile, true));
            
            ExecutionProfileDialogUI dialogUI = new ExecutionProfileDialogUI(getContext(), "Edit execution profile", false, profile);
            dialogUI.setTextForOKButton("Update profile");
            dialogUI.showDialog();
            
            if (!dialogUI.isOkPressed()) {
                continue;
            }
            /* ------------------------ */
            /* Update as wished by user: */
            /* ------------------------ */
            TestExecutionProfile updatedProfile = dialogUI.getUpdatedProfile();
            administration.updateExecutionProfile(updatedProfile);
            
            outputAsBeautifiedJSONOnSuccess("Updated execution profile:\n" + JSONConverter.get().toJSON(updatedProfile, true));
        }
    }
    
   

}