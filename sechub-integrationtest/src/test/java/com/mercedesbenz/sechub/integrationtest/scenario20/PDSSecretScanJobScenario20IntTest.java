// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario20;

import static com.mercedesbenz.sechub.commons.model.TrafficLight.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario20.Scenario20.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestJSONLocation;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario20.class)
public class PDSSecretScanJobScenario20IntTest {

    public static final String PATH_ZIPFILE_WITH_DATA_SECTION = "pds/secretscan/upload/zipfile_contains_inttest_secretscan_with_gitleaks_sample_sarif.json.zip";
    public static final String PATH_ZIPFILE_ONLY_ROOT_LEVEL = "pds/secretscan/upload/zipfile_contains_inttest_secretscan_with_gitleaks_sample_sarif_archive_root.json.zip";

    @Test
    void run_pds_secret_scan_and_download_report_via_rest_mark_finding_0_as_false_positive_and_ensure_next_scan_this_is_ignored() {
        /* prepare */
        String configurationAsJson = IntegrationTestFileSupport.getTestfileSupport()
                .loadTestFile(IntegrationTestJSONLocation.CLIENT_JSON_SECRET_SCAN_YELLOW_ZERO_WAIT.getPath());
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(configurationAsJson);

        configuration.setProjectId("myTestProject");

        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createJobAndReturnJobUUID(project, configuration);

        /* execute 1 */
        as(USER_1).uploadSourcecode(project, jobUUID, PATH_ZIPFILE_WITH_DATA_SECTION).approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID, 30, true);

        /* test 1 */
        String secretScanReport = as(USER_1).getJobReport(project, jobUUID);
        storeTestReport("report_pds_secretscan-1.json", secretScanReport);

        int expectedFindingId = 1;

        /* @formatter:off */
        assertReport(secretScanReport).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0).
            hasJobUUID(jobUUID).
            hasTrafficLight(YELLOW). // It is yellow because the SARIF default level is "warning" when not explicitly defined
            hasFindings(6).
    	        finding(0).
    	          hasId(expectedFindingId).
    	          hasRevisionId("b3816fddcf28aa29d94b10ec305cd52be14c472b"). //remark this is defined inside "zipfile_contains_inttest_secretscan_with_gitleaks_sample_sarif.json.zip"
    	          hasScanType(ScanType.SECRET_SCAN).
    	          hasDescription("generic-api-key has detected secret for file UnSAFE_Bank/Backend/docker-compose.yml.").
    	          hasName("Generic API Key").
    	          codeCall(0).
    	              hasColumn(14).
    	              hasLine(12).
    	              hasSource("*****").
    	              hasLocation("UnSAFE_Bank/Backend/docker-compose.yml").
    	          andFinding().
    	          hasCweId(798); // gitleak has no cwe id, but importer will do fallback to CWE 798 - see https://cwe.mitre.org/data/definitions/798.html

        String htmlReport = as(USER_1).
                    enableAutoDumpForHTMLReports().
                    getHTMLJobReport(project, jobUUID);
        storeTestReport("report_pds_secretscan-1.html", htmlReport);

        assertHTMLReport(htmlReport).
            containsAtLeastOneOpenDetailsBlock();

        /* execute 2 - mark as false positive */
        as(USER_1).startFalsePositiveDefinition(project).add(expectedFindingId, jobUUID).markAsFalsePositive();

        /* execute 3 */
        UUID jobUUID2 = as(USER_1).createJobAndReturnJobUUID(project, configuration);
        as(USER_1).uploadSourcecode(project, jobUUID2, PATH_ZIPFILE_WITH_DATA_SECTION).approveJob(project, jobUUID2);
        waitForJobDone(project, jobUUID2, 30, true);

        /* test 3 - the secret finding is marked as false positive and may no longer appear */
        String secretScanReport2 = as(USER_1).getJobReport(project, jobUUID2);

        assertReport(secretScanReport2).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0).
            hasJobUUID(jobUUID2).
            hasTrafficLight(YELLOW).
            hasFindings(5).
                finding(0).
                  hasNotId(expectedFindingId); // finding with id=1 has been removed by false positive recognition

        /* @formatter:on */

    }

    @Test
    void start_secretscan_job_with_custom_zipfile_containing_only_root_and_no_data_section_and_referencing_archive_root_in_config() {
        /* prepare */
        String configurationAsJson = IntegrationTestFileSupport.getTestfileSupport()
                .loadTestFile(IntegrationTestJSONLocation.CLIENT_JSON_SECRET_SCAN_YELLOW_ZERO_WAIT_WITH_SOURCECODE_ARCHIVE_ROOT_REFERENCE.getPath());
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(configurationAsJson);

        configuration.setProjectId("myTestProject");

        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createJobAndReturnJobUUID(project, configuration);

        /* execute 1 */
        as(USER_1).uploadSourcecode(project, jobUUID, PATH_ZIPFILE_ONLY_ROOT_LEVEL).approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID, 30, true);

        /* test 1 */
        String secretScanReport = as(USER_1).getJobReport(project, jobUUID);

        int expectedFindingId = 1;

        /* @formatter:off */
        assertReport(secretScanReport).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0).
            hasJobUUID(jobUUID).
            hasTrafficLight(YELLOW). // It is yellow because the SARIF default level is "warning" when not explicitly defined
            hasFindings(6).
                finding(0).
                  hasId(expectedFindingId).
                  hasRevisionId("b3816fddcf28aa29d94b10ec305cd52be14c472b"). //remark this is defined inside "zipfile_contains_inttest_secretscan_with_gitleaks_sample_sarif_archive_root.json.zip"
                  hasScanType(ScanType.SECRET_SCAN).
                  hasDescription("generic-api-key has detected secret for file UnSAFE_Bank/Backend/docker-compose.yml.").
                  hasName("Generic API Key").
                  codeCall(0).
                      hasColumn(14).
                      hasLine(12).
                      hasSource("*****").
                      hasLocation("UnSAFE_Bank/Backend/docker-compose.yml").
                  andFinding().
                  hasCweId(798); // gitleak has no cwe id, but importer will do fallback to CWE 798 - see https://cwe.mitre.org/data/definitions/798.html
        /* @formatter:on */
    }

}
