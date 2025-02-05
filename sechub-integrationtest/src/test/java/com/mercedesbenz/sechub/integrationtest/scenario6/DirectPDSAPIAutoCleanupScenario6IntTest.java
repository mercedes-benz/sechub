// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario6;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData.TestCleanupTimeUnit;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario6.class)
@Timeout(unit = TimeUnit.SECONDS, value = 600)
public class DirectPDSAPIAutoCleanupScenario6IntTest {

    @Test
    void auto_cleanup_executed_when_admin_configures_cleanupdays_1() {
        /* prepare */
        TestAutoCleanupData data = new TestAutoCleanupData(1, TestCleanupTimeUnit.DAY);

        /* execute */
        asPDSUser(PDS_ADMIN).updateAutoCleanupConfiguration(data);

        /* test */
        /* @formatter:off */

        waitUntilPDSAutoCleanupConfigurationChangedTo(data);
        resetPDSIntegrationTestAutoCleanupInspector();

        assertPDSAutoCleanupInspections().

            // inspection must contain the info that delete for job has been run and has not deleted anything
            // this just tests, that the cleanup service has been called by time trigger!
            // the logic inside the PDSAutoCleanupService itself is tested in another unit test!
            addExpectedDeleteInspection("pds-job","com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupService",0).

            addExpectedDifferentKindOfDeleteInspections(1).

            assertAsExpectedWithTimeOut(15);

        /* @formatter:on */

    }

    @AfterEach
    void afterEach() {
        /* disable */
        resetPDSAutoCleanupDaysToZero();
    }

}
