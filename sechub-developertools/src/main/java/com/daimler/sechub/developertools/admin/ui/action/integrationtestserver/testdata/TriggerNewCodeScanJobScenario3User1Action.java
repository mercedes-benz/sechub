// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata;

import java.awt.event.ActionEvent;
import java.util.UUID;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.IntegrationTestAction;
import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.scenario3.Scenario3;

public class TriggerNewCodeScanJobScenario3User1Action extends IntegrationTestAction {
	private static final long serialVersionUID = 1L;
	private IntegrationTestMockMode mode;

	public TriggerNewCodeScanJobScenario3User1Action(UIContext context, IntegrationTestMockMode mode) {
		super("Trigger new code scan job (Scenario3) -" + mode, context);
		this.mode = mode;
	}

	@Override
	protected void executeImplAfterRestHelperSwitched(ActionEvent e) {
		UUID uuid = TestAPI.as(Scenario3.USER_1).createCodeScan(Scenario3.PROJECT_1, mode);
		outputAsTextOnSuccess("Job created:" + uuid);
		TestAPI.as(Scenario3.USER_1).upload(Scenario3.PROJECT_1, uuid, "zipfile_contains_only_test1.txt.zip").approveJob(Scenario3.PROJECT_1, uuid);
		outputAsTextOnSuccess("Job uploaded and approved:" + uuid);
	}

}
