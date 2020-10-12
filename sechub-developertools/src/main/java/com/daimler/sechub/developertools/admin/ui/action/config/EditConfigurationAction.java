// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.UUID;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class EditConfigurationAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public EditConfigurationAction(UIContext context) {
		super("Edit executor configuration",context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> configUUID = getUserInput("Please enter config uuid", InputCacheIdentifier.EXECUTOR_CONFIG_UUID);
		if (! configUUID.isPresent()) {
			return;
		}
		UUID uuid = UUID.fromString(configUUID.get());
        TestExecutorConfig config = getContext().getAdministration().fetchExecutorConfiguration(uuid);
		
		 /* dump to output */
        outputAsTextOnSuccess("Config:"+uuid+" ass JSON:\n"+JSONConverter.get().toJSON(config,true));
       
        
		ExecutorConfigDialogUI ui = new ExecutorConfigDialogUI(getContext(),"Edit existing executor config", config);
        ui.showDialog();
        
        if (!ui.isOkPressed()) {
            return;
        }
        TestExecutorConfig updatedConfig = ui.getUpdatedConfig();
        
        getContext().getAdministration().updateExecutorConfiguration(updatedConfig);
        outputAsTextOnSuccess("executor config updated:\n"+JSONConverter.get().toJSON(updatedConfig,true));
	}

}