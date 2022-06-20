// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver.testdata.CreateScenario2TestDataAction;
import com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver.testdata.CreateScenario3TestDataAction;
import com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver.testdata.TriggerMassiveNewJobsScenario3User1Action;
import com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver.testdata.TriggerNewCodeScanJobScenario3User1Action;
import com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver.testdata.TriggerNewInfraScanJobScenario3User1Action;
import com.mercedesbenz.sechub.developertools.admin.ui.action.integrationtestserver.testdata.TriggerNewWebScanJobScenario3User1Action;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode;

public class IntegrationTestDataMenuAppender {

    public void appendMenuEntries(UIContext context, JMenu testDataMenu) {
        assertMockDataAvailable();

        add(testDataMenu, new CreateScenario2TestDataAction(context));
        add(testDataMenu, new CreateScenario3TestDataAction(context));
        testDataMenu.addSeparator();
        add(testDataMenu, new TriggerNewInfraScanJobScenario3User1Action(context));
        testDataMenu.addSeparator();
        add(testDataMenu, new TriggerNewWebScanJobScenario3User1Action(context, IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__ZERO_WAIT));
        add(testDataMenu, new TriggerNewWebScanJobScenario3User1Action(context, IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__10_SECONDS_WAITING));

        add(testDataMenu, new TriggerNewWebScanJobScenario3User1Action(context, IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__10_SECONDS_WAITING));

        add(testDataMenu, new TriggerNewWebScanJobScenario3User1Action(context, IntegrationTestMockMode.WEBSCAN__NETSPARKER_RED__ZERO_WAIT));
        add(testDataMenu, new TriggerNewWebScanJobScenario3User1Action(context, IntegrationTestMockMode.WEBSCAN__NETSPARKER_MULTI__ZERO_WAIT));
        testDataMenu.addSeparator();
        add(testDataMenu, new TriggerNewCodeScanJobScenario3User1Action(context, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__MULTI__ZERO_WAIT));
        add(testDataMenu, new TriggerNewCodeScanJobScenario3User1Action(context, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__1_SECOND_WAITING));
        testDataMenu.addSeparator();
        add(testDataMenu, new TriggerMassiveNewJobsScenario3User1Action(context));
    }

    private void assertMockDataAvailable() {
        /*
         * triggering next method, ensures that we have access to mock data - if not,
         * there will be an exception (which is caught at caller side)
         */
        IntegrationTestMockMode.values();
    }

    private void add(JMenu menu, AbstractAction action) {
        JMenuItem menuItem = new JMenuItem(action);
        menu.add(menuItem);

    }

}
