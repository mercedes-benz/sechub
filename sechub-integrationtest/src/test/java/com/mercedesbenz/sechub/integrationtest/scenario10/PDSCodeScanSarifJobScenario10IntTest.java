// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario10;

import static com.mercedesbenz.sechub.commons.model.TrafficLight.*;
import static com.mercedesbenz.sechub.integrationtest.api.AssertJob.*;
import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario10.Scenario10.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;

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
            uploadSourcecode(project, jobUUID, PATH).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID,10, true);

        /* test */
        // test storage is a sechub storage and no PDS storage
        String storagePath = fetchStoragePathHistoryEntryoForSecHubJobUUID(jobUUID); // this is a SecHub job UUID!
        assertNull("Storage path may not be found for SecHub jobUUID, "
                + "because storage must be done by PDS itself. But found for "+jobUUID+" path="+storagePath+
                " - wrong storage used!", storagePath); // storage path must NOT be found for SecHub job UUID because PDS storage with PDS jobUUID must be used!

        // test content as expected
        String report = as(USER_1).getJobReport(project, jobUUID);
        storeTestReport("report_pds_codescan_sarif-1.json", report);

        assertReport(report).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0).
            hasJobUUID(jobUUID).
            hasMetaDataLabel("quality-level", "high").
            hasMetaDataLabel("test-label1", "Something special").
            hasMetaDataLabel("test-label2", "").
            hasMetaDataLabel("test-label3_with_html", "<html>HTML is allowed, but must always be escaped in reports!</html>").
            hasMetaDataLabel("test-label4_with_special_chars", "Line1\nLine2\tLine3").
            hasTrafficLight(RED).
               finding(0).
                   hasSeverity(Severity.HIGH).
                   hasScanType(ScanType.CODE_SCAN).
                   hasName("BRAKE0102").
                   hasDescription("Rails 5.0.0 `content_tag` does not escape double quotes in attribute values (CVE-2016-6316). Upgrade to Rails 5.0.0.1.").
                   codeCall(0).
                      hasLocation("Gemfile.lock").
                      hasLine(115).
               andFinding(28). // 28 because the findings are sorted
                   hasName("BRAKE0116").
                   hasScanType(ScanType.CODE_SCAN).
                   hasSeverity(Severity.MEDIUM).
                   hasDescription("Rails 5.0.0 has a vulnerability that may allow CSRF token forgery. Upgrade to Rails 5.2.4.3 or patch.");

        String htmlReport = as(USER_1).
                enableAutoDumpForHTMLReports().
                getHTMLJobReport(project, jobUUID);
        storeTestReport("report_pds_codescan_sarif-1.html", htmlReport);

        assertHTMLReport(htmlReport).
            containsAtLeastOneOpenDetailsBlock().
            hasMetaDataLabel("quality-level", "high").
            hasMetaDataLabel("test-label1", "Something special").
            hasMetaDataLabel("test-label2", "").
            hasMetaDataLabel("test-label3_with_html", "&lt;html&gt;HTML is allowed, but must always be escaped in reports!&lt;/html&gt;").
            hasMetaDataLabel("test-label4_with_special_chars", "Line1\nLine2\tLine3");

        // try to restart SecHub (will reuse existing PDS job because already done )
        assertSecHubRestartWillNotStartNewJobButReusesExistingBecausePDSJobWasAlreadyDone(project,jobUUID);


        /* @formatter:on */
    }

    private void assertSecHubRestartWillNotStartNewJobButReusesExistingBecausePDSJobWasAlreadyDone(TestProject project, UUID jobUUID) {
        List<UUID> pdsJobUUIDs = TestAPI.fetchAllPDSJobUUIDsForSecHubJob(jobUUID);
        assertEquals(1, pdsJobUUIDs.size());
        UUID pdsJobUUID = pdsJobUUIDs.iterator().next();
        assertPDSJobStatus(pdsJobUUID).isInState(PDSJobStatusState.DONE);
        /* @formatter:on */

        assertNotNull(pdsJobUUID);

        revertJobToStillRunning(jobUUID); // fake it's running (so we can restart)
        assertJobIsRunning(project, jobUUID);
        as(SUPER_ADMIN).restartJobAndFetchJobStatus(project, jobUUID);

        List<UUID> pdsJobUUIDs2 = TestAPI.fetchAllPDSJobUUIDsForSecHubJob(jobUUID);

        UUID pdsJobUUID2 = pdsJobUUIDs2.iterator().next();

        assertNotNull(pdsJobUUID2);
        assertEquals("The PDS job was not reused, but new one was created! Execution in PDS was done before, so a new pds job maye not be started!", pdsJobUUID,
                pdsJobUUID2);
    }

}
