// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;

public class SchedulerDefaultStrategyScenario2IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(30);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Start scan job ...........................+ */
    /* +-----------------------------------------------------------------------+ */

    @Test
    public void when_no_scheduler_defined_fifo_scheduler_works_as_default() {
        /* @formatter:off */

        // set to nonsense to ensure it falls back to default
        TestAPI.switchSchedulerStrategy("nonsense");

        /* prepare */
        as(SUPER_ADMIN).
            assignUserToProject(USER_1, PROJECT_1);

        /* execute */
        UUID jobId1 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_1, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__4_SECONDS_WAITING);
        UUID jobId2 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_1, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__4_SECONDS_WAITING);

        waitSeconds(1);

        /* test */
        assertUser(SUPER_ADMIN).
            onJobAdministration().
            canFindRunningJob(jobId1);

        assertUser(SUPER_ADMIN).
            onJobAdministration().
            canFindRunningJob(jobId2);

        /* @formatter:on */
    }
}
