// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario10;

import static com.daimler.sechub.commons.model.TrafficLight.*;
import static com.daimler.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario10.Scenario10.*;
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
public class PDSCodeScanSarifJobScenario10IntTest {

    public static final String PATH = "pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical_sarif.zip";

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario10.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    @Test
    public void a_user_can_start_a_pds_sarif_scan_and_get_the_sarif_results_transformed_to_sechub_no_sechub_storage_reusage() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project,NOT_MOCKED);// scenario10 uses really integration test pds server! but WITHOUT reusage of sechub storage
        
        
        /* execute */
        as(USER_1).
            upload(project, jobUUID, PATH).
            approveJob(project, jobUUID);
        
        waitForJobDone(project, jobUUID,30);
        
        /* test */
        // test storage is a sechub storage and no PDS storage
        String storagePath = getPDSStoragePathForJobUUID(jobUUID); // this is a SecHub job UUID!
        assertNull("Storage path may not be found for SecHub jobUUID, "
                + "because storage must be done by PDS itself. But found for "+jobUUID+" path="+storagePath+
                " - wrong storage used!", storagePath); // storage path must NOT be found for SecHub job UUID because PDS storage with PDS jobUUID must be used! 
        
        // test content as expected
        String report = as(USER_1).getJobReport(project, jobUUID);
        assertReport(report).
            dump().
            hasTrafficLight(RED).
               finding(0).
                   hasSeverity(Severity.HIGH).
                   hasScanType(ScanType.CODE_SCAN).
                   hasName("Cross-Site Scripting").
                   hasDescription("Checks for XSS in calls to content_tag.").
                   codeCall(0).
                      hasLocation("Gemfile.lock").
                      hasLine(115).
               andFinding(1).
                   hasName("Cross-Site Request Forgery").
                   hasScanType(ScanType.CODE_SCAN).
                   hasSeverity(Severity.MEDIUM).
                   hasDescription("Checks for versions with CSRF token forgery vulnerability (CVE-2020-8166).");
        
        
        /* @formatter:on */
    }

}
