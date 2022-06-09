// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario6;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData.TestCleanupTimeUnit;

public class DirectPDSAPIAutoCleanupScenario6IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario6.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void auto_cleanup_executed_when_admin_configures_cleanupdays_1() {
        /* prepare */
        TestAutoCleanupData data = new TestAutoCleanupData(1, TestCleanupTimeUnit.DAY);
        /* execute */
        asPDSUser(PDS_ADMIN).updateAutoCleanupConfiguration(data);

        /* test */
        /* @formatter:off */

        waitUntilPDSAutoCleanupConfigurationChangedTo(data);
        resetPDSIntegrationTestAutoCleanupInspector();

        assertPDSAutoCleanupInspections().

            // inspection must contain the info that delte for job has been run and has not deleted anything
            addExpectedDeleteInspection("pds-job","com.mercedesbenz.sechub.pds.autocleanup.AdministrationAutoCleanupService",0).

            addExpectedDifferentKindOfDeleteInspections(1).

            assertAsExpectedWithTimeOut(15);

        /* @formatter:on */

    }

    @AfterAll
    static void afterAll() {
        /* disable */
        resetPDSAutoCleanupDaysToZero();
    }

}
