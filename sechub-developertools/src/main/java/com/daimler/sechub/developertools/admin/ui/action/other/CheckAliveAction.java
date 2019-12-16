// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.other;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;

public class CheckAliveAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public CheckAliveAction(UIContext context) {
		super("Check alive",context);
	}

	@Override
	public void execute(ActionEvent e) {
		String infoMessage = getContext().getAdministration().checkAlive();
		outputAsText(infoMessage);
	}

}