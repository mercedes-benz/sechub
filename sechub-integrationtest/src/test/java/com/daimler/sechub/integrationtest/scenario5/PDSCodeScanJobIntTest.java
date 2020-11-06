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

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;
/**
 * Integration test doing code scans by integration test servers (sechub server, pds server)
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
        UUID jobUUID = as(USER_1).createCodeScan(project,NOT_MOCKED);// scenario5 uses really integration test pds server!
        
        
        /* execute */
        as(USER_1).
            upload(project, jobUUID, "pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical.zip").
            approveJob(project, jobUUID);
        
        waitForJobDone(project, jobUUID,30);
        
        /* test */
        String report = as(USER_1).getJobReport(project, jobUUID);
        assertSecHubReport(report).
            hasTrafficLight(RED).
                finding().
                  scanType(ScanType.CODE_SCAN).
                  severity(Severity.CRITICAL).
                  description("i am a critical error").
                 isContained().
                finding().
                  scanType(ScanType.CODE_SCAN).
                  severity(Severity.MEDIUM).
                  description("i am a medium error").
                 isContained().  
                finding().
                  scanType(ScanType.CODE_SCAN).
                  severity(Severity.INFO).
                  description("i am just an information").
                 isContained();

            
        
    }
    /* @formatter:on */

    
}
