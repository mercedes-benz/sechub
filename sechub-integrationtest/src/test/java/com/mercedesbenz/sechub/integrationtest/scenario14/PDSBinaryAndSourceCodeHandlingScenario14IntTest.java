// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario14;

import static com.mercedesbenz.sechub.commons.model.TrafficLight.*;
import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario14.Scenario14.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TemplateData;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;

public class PDSBinaryAndSourceCodeHandlingScenario14IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario14.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    /**
     * Uses upload file:
     * {@link IntegrationTestExampleConstants#PATH_TO_TARFILE_WITH_DIFFERENT_DATA_SECTIONS}
     *
     * and setting of "files-a" which will result in:
     *
     * <pre>
     *   /__data__
     * files-a
     *   file-a-1.txt (low finding)
     *   file-a-2.txt (low finding)
     *   subfolder-1/
     *       file-a-3.txt ((medium finding)
     * </pre>
     *
     * Also one info finding will be added by script per default
     */
    @Test
    public void pds_upload_binaries_extracts_automatically_only_wanted_parts_config_references_criticial_id() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScanWithTemplate(
                              IntegrationTestTemplateFile.CODE_SCAN_2_BINARIES_DATA_ONE_REFERENCE,
                              project, NOT_MOCKED,
                              TemplateData.builder().addReferenceId("files-a").build());

        /* execute */
        as(USER_1).
            uploadBinaries(project, jobUUID, PATH_TO_TARFILE_WITH_DIFFERENT_DATA_SECTIONS).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID,30,true);

        /* test */
        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0).
            hasTrafficLight(YELLOW).
            hasFindings(4).
            assertUnordered(). // test unordered to avoid flaky tests because of file walk trough different data sections - result can change...
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.INFO).
                    description("pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=a,product1.level as PRODUCT1_LEVEL=42").
                    isContained().
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.LOW).
                    description("path-info=files-a/file-a-1.txt").
                    isContained().
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.LOW).
                    description("path-info=files-a/file-a-2.txt").
                    isContained().
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.MEDIUM).
                    description("path-info=files-a/subfolder-1/file-a-3.txt");
        /* @formatter:on */
    }

    /**
     * Uses upload file:
     * {@link IntegrationTestExampleConstants#PATH_TO_ZIPFILE_WITH_DIFFERENT_DATA_SECTIONS}
     *
     * and setting of "medium-id" which will result in:
     *
     * <pre>
     *   /__data__
     *     /medium-id
     *        data.txt (contains 3 findings: 1xmedium, 1xlow, 1xinfo)
     * </pre>
     *
     * Also one info finding will be added by script per default
     */
    @Test
    public void pds_upload_sources_extracts_automatically_only_wanted_parts_config_references_criticial_id() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScanWithTemplate(
                IntegrationTestTemplateFile.CODE_SCAN_3_SOURCES_DATA_ONE_REFERENCE,
                project, NOT_MOCKED,
                TemplateData.builder().addReferenceId("medium-id").build());

        /* execute */
        as(USER_1).
            uploadSourcecode(project, jobUUID, PATH_TO_ZIPFILE_WITH_DIFFERENT_DATA_SECTIONS).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID,30,true);

        /* test */
        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0).
            hasTrafficLight(YELLOW).
            hasFindings(4).
            assertUnordered(). // test unordered to avoid flaky tests because of file walk trough different data sections - result can change...
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.INFO).
                    description("pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=a,product1.level as PRODUCT1_LEVEL=42").
                    isContained().
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.INFO).
                    description("i am just an information").
                    isContained().
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.LOW).
                    description("i am just a low error").
                    isContained().
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.MEDIUM).
                    description("i am a medium error");

        assertPDSJob(TestAPI.assertAndFetchPDSJobUUIDForSecHubJob(jobUUID)).
            containsVariableTestOutput("PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED",true);


        /* @formatter:on */
    }

}
