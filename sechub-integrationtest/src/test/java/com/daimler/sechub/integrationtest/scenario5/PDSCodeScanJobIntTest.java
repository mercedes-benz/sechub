// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario5;

import static com.daimler.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario5.Scenario5.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static com.daimler.sechub.commons.model.TrafficLight.*;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;
/**
 * Integration tests between int test sechub server and integration test PDS server
 * 
 * @author Albert Tregnaghi
 *
 */
public class PDSCodeScanJobIntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario5.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;


    @Test
    public void a_user_can_start_a_pds_scan_job_and_gets_result() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project,NOT_MOCKED);
        
        /* execute */
        as(USER_1).
            upload(project, jobUUID, "pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical.zip").
            approveJob(project, jobUUID);
        
        waitForJobDone(project, jobUUID);
        
        /* test */
        String report = as(USER_1).getJobReport(project, jobUUID);
        assertJobReport(report).hasTrafficLight(RED);
        
    }
    /* @formatter:on */

    
}
