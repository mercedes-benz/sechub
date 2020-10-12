// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;

public class EditExecutionProfileAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public EditExecutionProfileAction(UIContext context) {
        super("Edit execution profile", context);
    }

    @Override
	public void execute(ActionEvent e) {
	    Optional<String> opt = getUserInput("Please enter profileId for profile to show", InputCacheIdentifier.EXECUTION_PROFILE_ID);
        if (!opt.isPresent()) {
            return;
        }
        String profileId=opt.get().trim();
        
        TestExecutionProfile profile = getContext().getAdministration().fetchExecutionProfile(profileId);
        
        /* dump to output */
        outputAsTextOnSuccess("Profile:"+profileId+" ass JSON:\n"+JSONConverter.get().toJSON(profile,true));
        
        
        ExecutionProfileDialogUI dialogUI = new ExecutionProfileDialogUI(getContext(),"Edit execution profile",false,profile);
	    
	    dialogUI.showDialog();
	    
	    if (!dialogUI.isOkPressed()) {
	        return; 
	    }
	    TestExecutionProfile updatedProfile = dialogUI.getUpdatedProfile();
	    getContext().getAdministration().updateExecutionProfile(updatedProfile);
        
		outputAsBeautifiedJSONOnSuccess("Updated execution profile:\n"+JSONConverter.get().toJSON(updatedProfile,true));
	}

}