// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver.testdata;

import java.awt.event.ActionEvent;

import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver.IntegrationTestAction;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.mercedesbenz.sechub.integrationtest.scenario3.Scenario3;

public class TriggerNewInfraScanJobScenario3User1Action extends IntegrationTestAction {
    private static final long serialVersionUID = 1L;

    public TriggerNewInfraScanJobScenario3User1Action(UIContext context) {
        super("Trigger new infra scan job (Scenario3) - " + Scenario3.PROJECT_1.getProjectId(), context);
    }

    @Override
    protected void executeImplAfterRestHelperSwitched(ActionEvent e) {
        ExecutionResult result = TestAPI.as(Scenario3.USER_1).withSecHubClient().createInfraScanAndFetchScanData(Scenario3.PROJECT_1);

        outputAsTextOnSuccess("Job executed, data fetched, last output line:" + result.getLastOutputLine());
        outputAsTextOnSuccess("Job UID was:" + result.getSechubJobUUID());
    }

}
