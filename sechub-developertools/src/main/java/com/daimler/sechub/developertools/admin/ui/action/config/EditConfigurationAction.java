// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;
import java.util.UUID;

import javax.swing.Action;
import javax.swing.ImageIcon;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class EditConfigurationAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public EditConfigurationAction(UIContext context) {
		super("Edit executor configuration",context);
		 putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/icons/material-io/twotone_edit_black_18dp.png")));
	}

	@Override
	public void execute(ActionEvent e) {
	    while(true) {
	        ListExecutorConfigurationDialogUI dialogUI = new ListExecutorConfigurationDialogUI(getContext(), "Select configuration you want to edit");
	        dialogUI.setOkButtonText("Edit configuration");
	        dialogUI.showDialog();
	        if (!dialogUI.isOkPressed()) {
	            return;
	        }
	        UUID uuid = dialogUI.getSelectedValue();
	        if (uuid==null) {
	            continue;
	        }
	        executeDirectly(uuid);
	    }
	}

	/**
	 * Executes action
	 * @param uuid
	 * @return true when update was done, otherwise false
	 */
    public boolean executeDirectly(UUID uuid) {
        TestExecutorConfig config = getContext().getAdministration().fetchExecutorConfiguration(uuid);
		
		 /* dump to output */
        outputAsTextOnSuccess("Config:"+uuid+" ass JSON:\n"+JSONConverter.get().toJSON(config,true));
       
        
		ExecutorConfigDialogUI ui = new ExecutorConfigDialogUI(getContext(),"Edit existing executor config", config);
		ui.setTextForOKButton("Update configuration");
        ui.showDialog();
        
        if (!ui.isOkPressed()) {
            return false;
        }
        TestExecutorConfig updatedConfig = ui.getUpdatedConfig();
        if (updatedConfig==null) {
            return false;
        }
        
        getContext().getAdministration().updateExecutorConfiguration(updatedConfig);
        outputAsTextOnSuccess("executor config updated:\n"+JSONConverter.get().toJSON(updatedConfig,true));
        return true;
    }

}