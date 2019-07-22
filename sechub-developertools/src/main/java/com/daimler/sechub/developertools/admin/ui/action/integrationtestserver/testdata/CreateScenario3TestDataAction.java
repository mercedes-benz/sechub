// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.IntegrationTestAction;
import com.daimler.sechub.integrationtest.scenario3.Scenario3;

public class CreateScenario3TestDataAction extends IntegrationTestAction {
	private static final long serialVersionUID = 1L;

	public CreateScenario3TestDataAction(UIContext context) {
		super("Create TestData (Scenario3)", context);
	}


	@Override
	protected void executeImplAfterRestHelperSwitched(ActionEvent e) {
		Scenario3 s = new Scenario3();
		s.prepare("Developer","AdminUI");
	}

}