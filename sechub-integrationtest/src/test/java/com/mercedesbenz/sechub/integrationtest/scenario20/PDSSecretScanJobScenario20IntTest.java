// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario20;

import static com.mercedesbenz.sechub.commons.model.TrafficLight.GREEN;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.assertReport;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.waitForJobDone;
import static com.mercedesbenz.sechub.integrationtest.scenario20.Scenario20.PROJECT_1;
import static com.mercedesbenz.sechub.integrationtest.scenario20.Scenario20.USER_1;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;

public class PDSSecretScanJobScenario20IntTest {
    public static final String PATH = "pds/secretscan/upload/zipfile_contains_inttest_secretscan_with_gitleaks_sample_sarif.json.zip";

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario20.class);

    @Test
    public void test_the_secret_scan_module__start_a_new_scan_and_run_pds_secret_scan_and_download_report_via_rest() {
        /* prepare */
        String configurationAsJson = IntegrationTestFileSupport.getTestfileSupport().loadTestFile("sechub-integrationtest-secretscanconfig.json");
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(configurationAsJson);

        configuration.setProjectId("myTestProject");

        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createJobAndReturnJobUUID(project, configuration);

        /* execute */
        as(USER_1).uploadSourcecode(project, jobUUID, PATH).approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID, 30, true);

        /* test */
        String secretScanReport = as(USER_1).getJobReport(project, jobUUID);

        /* @formatter:off */
        assertReport(secretScanReport).
        hasStatus(SecHubStatus.SUCCESS).
        hasMessages(0).
        hasJobUUID(jobUUID).
        hasTrafficLight(GREEN).
        hasFindings(6).
	        finding(0).
	        hasScanType(ScanType.SECRET_SCAN).
	        hasDescription("generic-api-key has detected secret for file UnSAFE_Bank/Backend/docker-compose.yml.");
        /* @formatter:on */
    }
}
