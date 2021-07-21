// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario9;

import static com.daimler.sechub.commons.model.TrafficLight.*;
import static com.daimler.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario9.Scenario9.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestProject;

/**
 * Integration test doing code scans by integration test servers (sechub server,
 * pds server)
 * 
 * @author Albert Tregnaghi
 *
 */
public class PDSCodeScanSarifJobScenario9IntTest {

    public static final String PATH = "pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical_sarif.zip";

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario9.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    /**
     * Important: This test is only working when we have same storage for SECHUB and for PDS defined!
     */
    @Test
    public void pds_reuses_sechub_data__a_user_can_start_a_pds_sarif_scan_and_get_result() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project,NOT_MOCKED);// scenario9 uses really integration test pds server!
        
        
        /* execute */
        as(USER_1).
            upload(project, jobUUID, PATH).
            approveJob(project, jobUUID);
        
        waitForJobDone(project, jobUUID,30);
        
        /* test */
        // test storage is a SecHub storage and no PDS storage
        String storagePath = getPDSStoragePathForJobUUID(jobUUID); // this is a SecHub job UUID!
        assertNotNull("Storage path not found for SecHub job UUID:"+jobUUID+" - wrong storage used!",storagePath); // storage path must be found for sechub job uuid, 
        if (!storagePath.contains("jobstorage/"+project.getProjectId())){
            fail("unexpected jobstorage path found:"+storagePath);
        }
        
        // test content as expected
        
        
        String report = as(USER_1).getJobReport(project, jobUUID);
        assertReport(report).
            hasTrafficLight(RED).
               finding(0).
                   hasSeverity(Severity.HIGH).
                   hasScanType(ScanType.CODE_SCAN).
                   hasName("BRAKE0102").
                   hasDescription("Checks for XSS in calls to content_tag.").
                   codeCall(0).
                      hasLocation("Gemfile.lock").
                      hasLine(115).
               andFinding(1).
                   hasName("BRAKE0116").
                   hasScanType(ScanType.CODE_SCAN).
                   hasSeverity(Severity.MEDIUM).
                   hasDescription("Checks for versions with CSRF token forgery vulnerability (CVE-2020-8166).");
        
        /* @formatter:on */
    }

}
