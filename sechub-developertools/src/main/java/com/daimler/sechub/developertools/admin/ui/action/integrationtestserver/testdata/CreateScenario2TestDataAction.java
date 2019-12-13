// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata;

import java.awt.event.ActionEvent;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.IntegrationTestAction;
import com.daimler.sechub.integrationtest.scenario2.Scenario2;

public class CreateScenario2TestDataAction extends IntegrationTestAction {
	private static final long serialVersionUID = 1L;

	public CreateScenario2TestDataAction(UIContext context) {
		super("Create TestData (Scenario2)", context);
	}


	@Override
	protected void executeImplAfterRestHelperSwitched(ActionEvent e) {
		Scenario2 s = new Scenario2();
		s.prepare("Developer","AdminUI");
	}

}