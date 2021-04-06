// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.*;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.springframework.test.context.TestPropertySource;
import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.AssertJobScheduler.TestExecutionResult;
import com.daimler.sechub.integrationtest.api.AssertJobScheduler.TestExecutionState;

@TestPropertySource(properties = { "sechub.scheduler.strategy.id=only-one-scan-per-project-at-a-time" })
public class SchedulerOnlyOneScanPerProjectStrategyScenario2IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(30);

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Start scan job ...........................+ */
    /* +-----------------------------------------------------------------------+ */

    @Test
    public void when_only_one_scan_per_project_scheduler_defined_jobs_are_executed_sequential_for_same_project() {
        /* @formatter:off */
        
        /* prepare */
                
        as(SUPER_ADMIN).
            assignUserToProject(USER_1, PROJECT_1);
        
        /* execute */
        UUID jobId1 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_1, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);
        
        UUID jobId2 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_1, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);
        
        waitSeconds(1);
        
        /* test */
        
        waitForJobDone(PROJECT_1, jobId1);
        
        assertUser(SUPER_ADMIN).
            onJobScheduling(PROJECT_1).
            canFindJob(jobId1).
            havingExecutionResult(TestExecutionResult.OK);
        
        assertUser(SUPER_ADMIN).
            onJobAdministration().
            canNotFindRunningJob(jobId2);
        
        waitUntilNoLongerJobsRunning();
        
        assertUser(SUPER_ADMIN).
            onJobScheduling(PROJECT_1).
            canFindJob(jobId2).
            havingExecutionResult(TestExecutionResult.OK);
        
        /* @formatter:on */
    }

    @Test
    public void scheduler_defined_jobs_are_executed_sequential_for_same_project_and_parallel_for_other_project() {
        /* @formatter:off */
        
        /* prepare */
                
        as(SUPER_ADMIN).
            assignUserToProject(USER_1, PROJECT_1);
        
        as(SUPER_ADMIN).
        assignUserToProject(USER_1, PROJECT_2);
        
        /* execute */
        UUID project1jobId1 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_1, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);
        UUID project2jobId1 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_2, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);
        
        UUID project1jobId2 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_1, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);                
        UUID project2jobId2 = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(PROJECT_2, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);
        
        waitSeconds(1);
        
        /* test */
        
        waitForJobDone(PROJECT_1, project1jobId1);
        waitForJobDone(PROJECT_2, project2jobId1);
        
        assertUser(SUPER_ADMIN).
            onJobScheduling(PROJECT_1).
            canFindJob(project1jobId1).
            havingExecutionResult(TestExecutionResult.OK);
        
        assertUser(SUPER_ADMIN).
            onJobScheduling(PROJECT_2).
            canFindJob(project2jobId1).
            havingExecutionResult(TestExecutionResult.OK);
        
        waitForJobRunning(PROJECT_1, project1jobId2);
        waitForJobRunning(PROJECT_2, project2jobId2);
        
        assertUser(SUPER_ADMIN).
            onJobScheduling(PROJECT_1).
            canFindJob(project1jobId2).
            havingExecutionState(TestExecutionState.STARTED);
        
        assertUser(SUPER_ADMIN).
            onJobScheduling(PROJECT_1).
            canFindJob(project1jobId2).
            havingExecutionState(TestExecutionState.STARTED);            

        
        waitUntilNoLongerJobsRunning();
        
        assertUser(SUPER_ADMIN).
            onJobScheduling(PROJECT_1).
            canFindJob(project1jobId2).
            havingExecutionResult(TestExecutionResult.OK);
       
        assertUser(SUPER_ADMIN).
            onJobScheduling(PROJECT_2).
            canFindJob(project2jobId2).
            havingExecutionResult(TestExecutionResult.OK);
        /* @formatter:on */
    }

}
