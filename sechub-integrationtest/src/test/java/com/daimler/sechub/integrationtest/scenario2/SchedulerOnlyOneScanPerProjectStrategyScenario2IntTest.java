// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.AssertJob.assertJobHasNotRun;
import static com.daimler.sechub.integrationtest.api.AssertJob.assertJobIsRunning;
import static com.daimler.sechub.integrationtest.api.TestAPI.SUPER_ADMIN;
import static com.daimler.sechub.integrationtest.api.TestAPI.as;
import static com.daimler.sechub.integrationtest.api.TestAPI.waitForJobDone;
import static com.daimler.sechub.integrationtest.api.TestAPI.waitForJobRunning;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.PROJECT_1;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.PROJECT_2;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.USER_1;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;

public class SchedulerOnlyOneScanPerProjectStrategyScenario2IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(30);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Start scan job ...........................+ */
    /* +-----------------------------------------------------------------------+ */


    @Test
    public void project1_job1_project1_job2_project2_job3__job3_is_executed_before_job2() {
        /* @formatter:off */
        
        /* prepare */
        
        TestAPI.switchSchedulerStrategy("only-one-scan-per-project-at-a-time");
                
        as(SUPER_ADMIN).
            assignUserToProject(USER_1, PROJECT_1);
        
        as(SUPER_ADMIN).
            assignUserToProject(USER_1, PROJECT_2);
        
        /* execute */
        UUID project1job1 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_1, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);
        UUID project1job2 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_1, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);                
        UUID project2job3 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_2, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);
        
        /* test */
        // check job1 is running
        waitForJobRunning(PROJECT_1, project1job1);
        assertJobIsRunning(PROJECT_1, project1job1);
        assertJobHasNotRun(PROJECT_1, project1job2);
        
        // check job 3 and job1 are running but not job2 
        waitForJobRunning(PROJECT_2, project2job3);
        assertJobIsRunning(PROJECT_2, project2job3);
        assertJobIsRunning(PROJECT_1, project1job1);
        
        assertJobHasNotRun(PROJECT_1, project1job2); // not this job
        
        // check job 3 is executed when job1 has finished
        waitForJobDone(PROJECT_1, project1job1);
        waitForJobDone(PROJECT_1, project1job2); // wait job3 has been done as well
        
        TestAPI.switchSchedulerStrategy("first-come-first-serve");
        
        /* @formatter:on */
    }
}
