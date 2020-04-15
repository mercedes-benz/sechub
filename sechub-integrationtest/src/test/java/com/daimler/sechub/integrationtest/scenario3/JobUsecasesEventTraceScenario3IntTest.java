// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.AssertEventInspection;
import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;

public class JobUsecasesEventTraceScenario3IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test // use scenario3 because USER_1 is already assigned to PROJECT_1
    public void UC_ADMIN_UNASSIGNS_USER_FROM_PROJECT() {
        /* @formatter:off */
        /* check preconditions */
        assertUser(USER_1).isAssignedToProject(PROJECT_1).hasOwnerRole().hasUserRole();
        
	    /* prepare */
        TestAPI.startEventInspection();

        /* execute */
        UUID uuid = as(USER_1).createWebScan(PROJECT_1, IntegrationTestMockMode.WEBSCAN__NETSPARKER_RESULT_GREEN__LONG_RUNNING);
        assertNotNull(uuid);
        as(USER_1).approveJob(PROJECT_1, uuid);
        
        /* test */
        AssertEventInspection.assertEventInspectionFailsButGeneratesTestCaseProposal();
//        assertAsExpectedAndCreateHistoryFile(UseCaseIdentifier.UC_ADMIN_RESTARTS_JOB_HARD);
        /* @formatter:on */
    }

}
