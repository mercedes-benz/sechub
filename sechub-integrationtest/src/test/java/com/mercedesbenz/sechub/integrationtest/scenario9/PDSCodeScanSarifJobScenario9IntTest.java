// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario9;

import static com.mercedesbenz.sechub.commons.model.TrafficLight.*;
import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario9.Scenario9.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;

/**
 * Integration test doing code scans by integration test servers (SecHub server,
 * PDS server).
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
     * Important: This test is only working when we have same storage for SECHUB and
     * for PDS defined!
     */
    @Test
    public void pds_reuses_sechub_data__user_starts_webscan_marks_report_finding_1_as_fp_and_scans_again_without_this_finding() {
        /* @formatter:off */

        /* ---------------------------------------------*/
        /* Phase 1: WebScan without false positive data */
        /* ---------------------------------------------*/

        /* prepare 1*/
        TestProject project = PROJECT_1;

        UUID jobUUID = as(USER_1).createWebScan(project,NOT_MOCKED);// scenario9 uses real integration test pds server!

        /* execute 1*/
        as(USER_1).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID,30,true);

        /* test 1*/
        String report = as(USER_1).getJobReport(project, jobUUID);
        assertReport(report).
            hasStatus(SecHubStatus.SUCCESS).
            hasTrafficLight(RED).
            hasFindings(14).
               finding(0).
                   hasCweId(79).
                   hasId(1).
                   hasSeverity(Severity.HIGH).
                   hasScanType(ScanType.WEB_SCAN).
                   hasName("Cross Site Scripting (Reflected)").
                   hasDescriptionContaining("There are three types").
                   hasDescriptionContaining("DOM-based").
               finding(1).
                   hasCweId(693).
                   hasId(2).
                   hasName("CSP: Wildcard Directive").
                   hasScanType(ScanType.WEB_SCAN).
                   hasSeverity(Severity.MEDIUM).
                   hasDescriptionContaining("either allow wildcard sources");

        /* --------------------------------------------------------*/
        /* Phase 2: WebScan with false positive data definition set*/
        /*          Next web scan for same project does not contain*/
        /*          the formerly marked false positive             */
        /* --------------------------------------------------------*/

        /* prepare 2 */
        as(USER_1).startFalsePositiveDefinition(PROJECT_1).add(1, jobUUID).markAsFalsePositive();


        UUID jobUUID2 = as(USER_1).createWebScan(project,NOT_MOCKED);// scenario9 uses real integration test pds server!

        /* execute 2 */
        as(USER_1).
            approveJob(project, jobUUID2);

        waitForJobDone(project, jobUUID2,30,true);

        /* test 2 */
        String report2 = as(USER_1).getJobReport(project, jobUUID2);
        assertReport(report2).
            hasStatus(SecHubStatus.SUCCESS).
            hasTrafficLight(YELLOW).// high finding no longer in report but only medium ones...
            hasFindings(13).// report has one finding less
               finding(0).
                   hasCweId(693).
                   hasId(2). // finding 1 is still there, but a false positive... so first finding inside this report is still having id 2
                   hasName("CSP: Wildcard Directive").
                   hasScanType(ScanType.WEB_SCAN).
                   hasSeverity(Severity.MEDIUM).
                   hasDescriptionContaining("either allow wildcard sources");
        /* @formatter:on */
    }

    /**
     * Important: This test is only working when we have same storage for SECHUB and
     * for PDS defined!
     */
    @Test
    public void pds_reuses_sechub_data__a_user_can_start_a_pds_codescan_with_sarif_output_and_get_result() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project,NOT_MOCKED);// scenario9 uses real integration test pds server!


        /* execute */
        as(USER_1).
            uploadSourcecode(project, jobUUID, PATH).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID,30,true);

        /* test */
        // test storage is a SecHub storage and no PDS storage
        String storagePath = fetchStoragePathHistoryEntryoForSecHubJobUUID(jobUUID); // this is a SecHub job UUID!
        assertNotNull("Storage path not found for SecHub job UUID:"+jobUUID+" - wrong storage used!",storagePath); // storage path must be found for sechub job uuid,
        if (!storagePath.contains("jobstorage/"+project.getProjectId())){
            fail("unexpected jobstorage path found:"+storagePath);
        }

        // test content as expected


        String report = as(USER_1).getJobReport(project, jobUUID);
        assertReport(report).
            hasStatus(SecHubStatus.SUCCESS).
            hasTrafficLight(RED).
               finding(0).
                   hasSeverity(Severity.HIGH).
                   hasScanType(ScanType.CODE_SCAN).
                   hasName("BRAKE0102").
                   hasDescription("Rails 5.0.0 `content_tag` does not escape double quotes in attribute values (CVE-2016-6316). Upgrade to Rails 5.0.0.1.").
                   codeCall(0).
                      hasLocation("Gemfile.lock").
                      hasLine(115).
               andFinding(1).
                   hasName("BRAKE0116").
                   hasScanType(ScanType.CODE_SCAN).
                   hasSeverity(Severity.MEDIUM).
                   hasDescription("Rails 5.0.0 has a vulnerability that may allow CSRF token forgery. Upgrade to Rails 5.2.4.3 or patch.");


        // check script trust all is defined here with "false". Because PROFILE_3_PDS_CODESCAN_SARIF
        // uses PDS_V1_CODE_SCAN_D which has defined the parameter as false
        assertPDSJob(assertAndFetchPDSJobUUIDForSecHubJob(jobUUID)).
            containsVariableTestOutput("PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED", "false");


        /* @formatter:on */
    }

}
