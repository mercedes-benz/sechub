// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario21;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario21.Scenario21.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;

public class PDSSolutionMockModeScenario21IntTest {

    private static final Logger LOG = LoggerFactory.getLogger(PDSSolutionMockModeScenario21IntTest.class);

    private static final String REFERENCE_NAME1 = "reference-name1";

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario21.class);

    @Test
    public void pds_solution_gosec_mocked_report_in_json_and_html_available() throws Exception {
        executePDSSolutionJobAndStoreReports(ScanType.CODE_SCAN, PROJECT_1, "gosec");
    }

    @Test
    public void pds_solution_checkmarx_mocked_report_in_json_and_html_available() throws Exception {
        executePDSSolutionJobAndStoreReports(ScanType.CODE_SCAN, PROJECT_2, "checkmarx");
    }

    @Test
    public void pds_solution_multi_bandit_mocked_report_in_json_and_html_available() throws Exception {
        executePDSSolutionJobAndStoreReports(ScanType.CODE_SCAN, PROJECT_3, "multi_bandit");
    }

    @Test
    public void pds_solution_zap_mocked_report_in_json_and_html_available() throws Exception {
        executePDSSolutionJobAndStoreReports(ScanType.WEB_SCAN, PROJECT_4, "zap");
    }

    @Test
    public void pds_solution_scancode_spdx_mocked_report_in_json_and_html_available() throws Exception {
        executePDSSolutionJobAndStoreReports(ScanType.LICENSE_SCAN, PROJECT_5, "scancode");
    }

    @Test
    public void pds_solution_gitleaks_mocked_report_in_json_and_html_available() throws Exception {
        SecHubReportModel report = executePDSSolutionJobAndStoreReports(ScanType.SECRET_SCAN, PROJECT_6, "gitleaks");
        /* @formatter:off */
        assertReportUnordered(report.toJSON())
                    .hasTrafficLight(TrafficLight.RED)
                           .finding()
                           .severity(Severity.CRITICAL)
                           .scanType(ScanType.SECRET_SCAN)
                           .description("github-pat has detected secret for file UnSAFE_Bank/iOS/Source Code/Pods/README.adoc.")
                           .isContained();
        /* @formatter:on */
    }

    @Test
    public void pds_solution_tern_mocked_report_in_json_and_html_available() throws Exception {
        executePDSSolutionJobAndStoreReports(ScanType.LICENSE_SCAN, PROJECT_7, "tern");
    }

    @Test
    public void pds_solution_xray_spdx_mocked_report_in_json_and_html_available() throws Exception {
        executePDSSolutionJobAndStoreReports(ScanType.LICENSE_SCAN, PROJECT_8, "xray_spdx");
    }

    @Test
    @Ignore("Test is correct, but CycloneDX currently not by SecHub. Test should be re-activated when CycloneDX import is implemented")
    public void pds_solution_xray_cyclonedx_mocked_report_in_json_and_html_available() throws Exception {
        /*
         * TODO Albert Tregnaghi, 2023-12-14: Enable the test when we support CycloneDX
         * in SecHub
         */
        executePDSSolutionJobAndStoreReports(ScanType.LICENSE_SCAN, PROJECT_9, "xray_cyclonedx");
    }

    @Test
    public void pds_solution_findsecuritybugs_mocked_report_in_json_and_html_available() throws Exception {
        executePDSSolutionJobAndStoreReports(ScanType.CODE_SCAN, PROJECT_10, "findsecuritybugs");
    }

    @Test
    public void pds_solution_kics_mocked_report_in_json_and_html_available() throws Exception {
        /* execute + test */
        SecHubReportModel report = executePDSSolutionJobAndStoreReports(ScanType.IAC_SCAN, PROJECT_11, "kics");

        /* test */
        assertReportUnordered(report.toJSON()).hasTrafficLight(TrafficLight.RED).finding().severity(Severity.HIGH).scanType(ScanType.IAC_SCAN)
                .name("OSS Bucket Public Access Enabled").description("'acl' is public-read-write").isContained();
    }

    @Test
    public void pds_solution_zap_mocked_report_REST_API_direct_mark_and_unmark_false_positive_projectData_webscan() throws Exception {
        /* @formatter:off */

        /* prepare */
        // execute scan to see a HIGH finding inside the webscan report
        SecHubReportModel report1 = executePDSSolutionJobAndStoreReports(ScanType.WEB_SCAN, PROJECT_4, "zap");
        assertReportUnordered(report1.toJSON())
        .hasTrafficLight(TrafficLight.RED)
               .finding()
               .severity(Severity.HIGH)
               .scanType(ScanType.WEB_SCAN)
               .name("SQL Injection - SQLite")
               .description("RDBMS [SQLite] likely, given error message fragment [SQLITE_ERROR] in HTML results")
               .isContained();

        WebscanFalsePositiveProjectData webscan = new WebscanFalsePositiveProjectData();
        // we set only mandatory parameters
        webscan.setCweId(89);
        webscan.setUrlPattern("http://localhost:3000/rest/products/search*");

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        String projectDataId = "6a7fe94a-564f-11ef-87de-3f13a69f3e5d";
        projectData.setId(projectDataId);
        projectData.setWebScan(webscan);

        /* execute 1 */
        // mark a false positive via projectData that matches the previously detected HIGH finding
        as(USER_1).startFalsePositiveDefinition(PROJECT_4).add(projectData).markAsFalsePositive();
        SecHubReportModel report2 = executePDSSolutionJobAndStoreReports(ScanType.WEB_SCAN, PROJECT_4, "zap");

        /* test 1 */
        // since the HIGH finding was marked as false positive we expected it to not be inside the report after a second scan
        assertReportUnordered(report2.toJSON())
        .hasTrafficLight(TrafficLight.YELLOW)
               .finding()
               .severity(Severity.HIGH)
               .scanType(ScanType.WEB_SCAN)
               .name("SQL Injection - SQLite")
               .description("RDBMS [SQLite] likely, given error message fragment [SQLITE_ERROR] in HTML results")
               .isNotContained();

        /* execute 2 */
        // unmark the projectData via the ID and perform another scan
        as(USER_1).startFalsePositiveDefinition(PROJECT_4).add(projectDataId).unmarkFalsePositiveProjectData();
        SecHubReportModel report3 = executePDSSolutionJobAndStoreReports(ScanType.WEB_SCAN, PROJECT_4, "zap");

        /* test 2 */
        // since the projectData must have been deleted, the HIGH finding must be back inside the report
        assertReportUnordered(report3.toJSON())
        .hasTrafficLight(TrafficLight.RED)
               .finding()
               .severity(Severity.HIGH)
               .scanType(ScanType.WEB_SCAN)
               .name("SQL Injection - SQLite")
               .description("RDBMS [SQLite] likely, given error message fragment [SQLITE_ERROR] in HTML results")
               .isContained();

        /* @formatter:on */
    }

    private SecHubReportModel executePDSSolutionJobAndStoreReports(ScanType scanType, TestProject project, String solutionName) {
        SecHubConfigurationModel model = createTestModelFor(scanType, project);
        UUID jobUUID = as(USER_1).createJobAndReturnJobUUID(project, model);

        as(USER_1).approveJob(project, jobUUID);
        waitForJobDone(project, jobUUID, 30, true);

        /* execute */
        String reportName = "report_pds-solution_" + solutionName + "_mocked";
        String reportAsJson = as(USER_1).getJobReport(project, jobUUID);
        storeTestReport(reportName + ".json", reportAsJson);

        String reportAsHtml = as(USER_1).getHTMLJobReport(project, jobUUID);
        storeTestReport(reportName + ".html", reportAsHtml);

        SecHubReportModel report = JSONConverter.get().fromJSON(SecHubReportModel.class, reportAsJson);

        /* test */
        if (!SecHubStatus.SUCCESS.equals(report.getStatus())) {
            TestAPI.dumpAllPDSJobOutputsForSecHubJob(jobUUID);
            fail("Report has not status SUCCESS but: " + report.getStatus() + " - something was wrong. Look at the console output for details");
        }

        /*
         * now we do sanity check via info and warn messages - if something is wrong
         * configured in tests we can fail here
         */
        Set<SecHubMessage> messages = report.getMessages();
        if (messages.size() != 2) {
            LOG.error("Messages not as expected: {}", messages);
            fail("Messages count not as expected! Expected 2 but got " + messages.size() + "!\nMessages found:\n" + messages);
        }

        Iterator<SecHubMessage> iterator = messages.iterator();
        SecHubMessage message1 = iterator.next();
        SecHubMessage message2 = iterator.next();

        SecHubMessage productMessage = null;
        SecHubMessage mockMessage = null;
        if (SecHubMessageType.INFO.equals(message1.getType())) {
            productMessage = message1;
            mockMessage = message2;
        } else {
            productMessage = message2;
            mockMessage = message1;
        }

        if (!productMessage.getText().contains("product:" + solutionName)) {
            // we use assertEquals here to have the text output directly in our IDEs
            assertEquals("product info message should contain: 'product:" + solutionName + "' but did not!", productMessage.getText());
        }

        if (!mockMessage.getText().contains("mocked result")) {
            // we use assertEquals here to have the text output directly in our IDEs
            assertEquals("mock warn message should contain: 'mocked result' but did not!", mockMessage.getText());
        }

        if (ScanType.LICENSE_SCAN.equals(scanType)) {
            // we download the SPDX report
            String spdxReport = as(USER_1).getSpdxReport(project, jobUUID);
            storeTestReport(reportName + ".spdx.json", spdxReport);
        }
        return report;
    }

    private SecHubConfigurationModel createTestModelFor(ScanType type, TestProject project) {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        switch (type) {
        case CODE_SCAN:
            SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
            codeScan.getNamesOfUsedDataConfigurationObjects().add(REFERENCE_NAME1);
            model.setCodeScan(codeScan);
            break;
        case INFRA_SCAN:
            SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();
            try {
                infraScan.getUris().add(new URI("https://example.com"));
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Should not happen - test corrupt", e);
            }
            model.setInfraScan(infraScan);
            break;
        case LICENSE_SCAN:
            SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();
            licenseScan.getNamesOfUsedDataConfigurationObjects().add(REFERENCE_NAME1);
            model.setLicenseScan(licenseScan);
            break;
        case SECRET_SCAN:
            SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();
            secretScan.getNamesOfUsedDataConfigurationObjects().add(REFERENCE_NAME1);
            model.setSecretScan(secretScan);
            break;
        case IAC_SCAN:
            SecHubIacScanConfiguration iacScan = new SecHubIacScanConfiguration();
            iacScan.getNamesOfUsedDataConfigurationObjects().add(REFERENCE_NAME1);
            model.setIacScan(iacScan);
            break;
        case WEB_SCAN:
            SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
            try {
                // add whitelisted URI
                webScan.setUrl(new URI("http://locahost/" + project.getProjectId()));
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Should not happen - test corrupt", e);
            }
            model.setWebScan(webScan);
            break;
        default:
            throw new IllegalStateException("Not implemented/handled type:" + type);

        }

        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubSourceDataConfiguration source = new SecHubSourceDataConfiguration();
        source.setUniqueName(REFERENCE_NAME1);
        SecHubFileSystemConfiguration fileSystem = new SecHubFileSystemConfiguration();
        fileSystem.getFolders().add("source-folder1");
        source.setFileSystem(fileSystem);
        data.getSources().add(source);
        model.setData(data);

        model.setApiVersion("1.0");

        return model;
    }
}
