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

@TestPropertySource(properties = {"sechub.scheduler.strategy.id="})
public class SchedulerDefaultStrategyScenario2IntTest2 {
    
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
        
        /* prepare */
        as(SUPER_ADMIN).
            assignUserToProject(USER_1, PROJECT_1);
        
        /* execute */
        UUID jobId1 = assertUser(USER_1).
                canCreateWebScan(PROJECT_1, IntegrationTestMockMode.WEBSCAN__NETSPARKER_RESULT_GREEN__LONG_RUNNING);// we use long running job (10seconds) - necessary, see comment beyond
        UUID jobId2 = assertUser(USER_1).
                canCreateWebScan(PROJECT_1, IntegrationTestMockMode.WEBSCAN__NETSPARKER_RESULT_GREEN__LONG_RUNNING);// we use long running job (10seconds) - necessary, see comment beyond
        
        assertUser(USER_1).canApproveJob(PROJECT_1, jobId1);
        assertUser(USER_1).canApproveJob(PROJECT_1, jobId2);
        
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
