// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario5;

import static com.daimler.sechub.commons.model.TrafficLight.*;
import static com.daimler.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario5.Scenario5.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.internal.IntegrationTestDefaultExecutorConfigurations;

/**
 * Integration test doing code scans by integration test servers (sechub server, pds server)
 * 
 * @author Albert Tregnaghi
 *
 */
public class PDSCodeScanJobScenario5IntTest {

    public static final String PATH ="pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical.zip";
    
    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario5.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;


    @SuppressWarnings("deprecation") // we use assertSecHubReport here - old implementation okay here
    @Test
    public void a_user_can_start_a_pds_scan_job_and_gets_result_containing_expected_findings_and_also_dynamic_parts() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project,NOT_MOCKED);// scenario5 uses really integration test pds server!
        
        
        /* execute */
        as(USER_1).
            upload(project, jobUUID, PATH).
            approveJob(project, jobUUID);
        
        waitForJobDone(project, jobUUID,30);
        
        /* test */
        String report = as(USER_1).getJobReport(project, jobUUID);
        assertSecHubReport(report).
            hasTrafficLight(RED).
                // findings from uploaded zip (1:1 mapped by textfile:
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
                 isContained().
                // here comes dynamic parts:
                finding().
                  scanType(ScanType.CODE_SCAN).
                  severity(Severity.INFO).
                  // we check the parameters in next line: we are using variant a in this scenario, level is always 42, but given as job parameter and returned by integrationtest-codescan.sh
                  description("pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=a,product1.level as PRODUCT1_LEVEL="+IntegrationTestDefaultExecutorConfigurations.VALUE_PRODUCT_LEVEL).
                isContained();
    }
    
}
