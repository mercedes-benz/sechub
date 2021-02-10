// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario4;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario4.Scenario4.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;

/**
 * Integration tests to check cancel operations works
 * 
 * @author Albert Tregnaghi
 *
 */
public class CancelJobScenario4IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario4.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    /**
     * We start a long running job and start a cancel operation here
     */
    public void cancel_a_long_running_webscan_job() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncWebScanGreenLongRunningAndGetJobUUID(project);
        waitForJobRunning(project, sechubJobUUD);
        
        /* execute */
        as(SUPER_ADMIN).cancelJob(sechubJobUUD);
        
        /* test */
        waitForJobStatusCancelRequested(project, sechubJobUUD);
        waitForJobResultFailed(project, sechubJobUUD);
        
        
        /* @formatter:on */
    }
    
    @Test
    /**
     * We start a long running job and start a cancel operation here
     */
    public void cancel_a_long_running_codescan_job() {
        /* @formatter:off */
        /* prepare */
        UUID sechubJobUUD = as(USER_1).triggerAsyncCodeScanWithPseudoZipUpload(project,IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__LONG_RUNNING);
        waitForJobRunning(project, sechubJobUUD);
        
        /* execute */
        as(SUPER_ADMIN).cancelJob(sechubJobUUD);
        
        /* test */
        waitForJobStatusCancelRequested(project, sechubJobUUD);
        waitForJobResultFailed(project, sechubJobUUD);
        
        
        /* @formatter:on */
    }
    
}