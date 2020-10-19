// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;
import java.util.UUID;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class CreateExecutorConfigAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public CreateExecutorConfigAction(UIContext context) {
		super("Create executor config", context);
//		setIcon(getClass().getResource("/icons/material-io/twotone_add_black_18dp.png"));
	}

	@Override
	public void execute(ActionEvent e) {
		ExecutorConfigDialogUI ui = new ExecutorConfigDialogUI(getContext(),"Create NEW executor config");
		ui.showDialog();
		
		if (!ui.isOkPressed()) {
		    return;
		}
		
		UUID uuid = getContext().getAdministration().createExecutorConfig(ui.getUpdatedConfig());
		outputAsTextOnSuccess("executor config created with uuid:"+uuid);
	}

}