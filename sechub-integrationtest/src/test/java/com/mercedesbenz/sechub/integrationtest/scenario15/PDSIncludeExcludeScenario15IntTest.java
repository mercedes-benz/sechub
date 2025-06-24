// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario15;

import static com.mercedesbenz.sechub.commons.model.TrafficLight.*;
import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario15.Scenario15.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.TemplateData;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario15.class)
@Timeout(unit = TimeUnit.SECONDS, value = 30)
public class PDSIncludeExcludeScenario15IntTest {

    /**
     * Uses upload file:
     * {@link IntegrationTestExampleConstants#PATH_TO_TARFILE_WITH_DATA_SECTION_FOR_INCLUDE_EXCLUDES}
     *
     * and using section "files-b" in combination with the given includes and
     * excludes from profile 10. This will result in:
     *
     * <pre>
     *    /__data__
     *       /files-b/
     *         /included-folder
     *           file-b-1.txt (low finding)
     *           file-b-2.txt (low finding)
     *           subfolder-2/
     *              file-b-3.txt (critical finding)
     * </pre>
     *
     * Also one info finding will be added by script per default
     */
    @Test
    void pds_upload_binaries_extracts_automatically_only_wanted_parts_and_handles_includes_excludes() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScanWithTemplate(
                IntegrationTestTemplateFile.CODE_SCAN_2_BINARIES_DATA_ONE_REFERENCE,
                project, NOT_MOCKED,
                TemplateData.builder().addReferenceId("files-b").build());

        /* execute */
        as(USER_1).
            uploadBinaries(project, jobUUID, PATH_TO_TARFILE_WITH_DATA_SECTION_FOR_INCLUDE_EXCLUDES).
            approveJob(project, jobUUID);


        /* test */
        waitForJobDone(project, jobUUID,30,true);

        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0).
            hasTrafficLight(RED).
            hasFindings(4).
            assertUnordered(). // test unordered to avoid flaky tests because of file walk trough different data sections - result can change...
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.INFO).
                    description("pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=i,product1.level as PRODUCT1_LEVEL=42").
                    isContained().
               finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.LOW).
                    description("path-info=files-b/included-folder/file-b-1.txt").
                    isContained().
                    finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.LOW).
                    description("path-info=files-b/included-folder/file-b-2.txt").
                    isContained().
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.CRITICAL).
                    description("path-info=files-b/included-folder/subfolder-2/file-b-3.txt").
                    isContained();

        // check the script trust all for this variant (i) is not defined (missing in profile)
        assertPDSJob(TestAPI.assertAndFetchPDSJobUUIDForSecHubJob(jobUUID)).
            containsVariableTestOutput("PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED","");

        /* @formatter:on */
    }

    /**
     * Uses upload file (which has no __data__ section and will be referenced by
     * __binaries_archive_root__ identifier) :
     * {@link IntegrationTestExampleConstants#PATH_TO_TARFILE_WITH_DATA_SECTION_FOR_INCLUDE_EXCLUDES__BINARIES_ARCHIVE_ROOT_USED}
     *
     * and using section "files-b" in combination with the given includes and
     * excludes from profile 10. This will result in:
     *
     * <pre>
     *    /files-b/
     *         /included-folder
     *           file-b-1.txt (low finding)
     *           file-b-2.txt (low finding)
     *           subfolder-2/
     *              file-b-3.txt (critical finding)
     * </pre>
     *
     * Also one info finding will be added by script per default
     */
    @Test
    void pds_upload_binaries_extracts_automatically_only_wanted_parts_and_handles_includes_excludes__binaries_archive_root_used() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScanWithTemplate(
                IntegrationTestTemplateFile.CODE_SCAN_4_NO_DATA_SECTION_BUT_ONE_USAGE,
                project, NOT_MOCKED,
                TemplateData.builder().addReferenceId("__binaries_archive_root__").build());

        /* execute */
        as(USER_1).
            uploadBinaries(project, jobUUID, PATH_TO_TARFILE_WITH_DATA_SECTION_FOR_INCLUDE_EXCLUDES__BINARIES_ARCHIVE_ROOT_USED).
            approveJob(project, jobUUID);


        /* test */
        waitForJobDone(project, jobUUID,30,true);

        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0).
            hasTrafficLight(RED).
            hasFindings(4).
            assertUnordered(). // test unordered to avoid flaky tests because of file walk trough different data sections - result can change...
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.INFO).
                    description("pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=i,product1.level as PRODUCT1_LEVEL=42").
                    isContained().
               finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.LOW).
                    description("path-info=files-b/included-folder/file-b-1.txt").
                    isContained().
                    finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.LOW).
                    description("path-info=files-b/included-folder/file-b-2.txt").
                    isContained().
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.CRITICAL).
                    description("path-info=files-b/included-folder/subfolder-2/file-b-3.txt").
                    isContained();

        // check the script trust all for this variant (i) is not defined (missing in profile)
        assertPDSJob(TestAPI.assertAndFetchPDSJobUUIDForSecHubJob(jobUUID)).
            containsVariableTestOutput("PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED","");

        /* @formatter:on */
    }

    /**
     * Uses upload file:
     * {@link IntegrationTestExampleConstants#PATH_TO_TARFILE_WITH_DATA_SECTION_FOR_INCLUDE_EXCLUDES}
     *
     * and using section "files-b" in combination with the given includes and
     * excludes from profile 10. This will result in:
     *
     * <pre>
     *    /__data__
     *       /files-b/
     *         /included-folder
     *           file-b-1.txt (low finding)
     *           file-b-2.txt (low finding)
     *           subfolder-2/
     *              file-b-3.txt (critical finding)
     * </pre>
     *
     * Also one info finding will be added by script per default
     */
    @Test
    void pds_upload_sources_extracts_automatically_only_wanted_parts_and_handles_includes_excludes() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScanWithTemplate(
                IntegrationTestTemplateFile.CODE_SCAN_3_SOURCES_DATA_ONE_REFERENCE,
                project, NOT_MOCKED,
                TemplateData.builder().addReferenceId("files-b").build());

        /* execute */
        as(USER_1).
            uploadSourcecode(project, jobUUID, PATH_TO_ZIPFILE_WITH_DATA_SECTION_FOR_INCLUDE_EXCLUDES).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID,30,true);

        /* test */
        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0).
            hasTrafficLight(RED).
            hasFindings(4).
            assertUnordered(). // test unordered to avoid flaky tests because of file walk trough different data sections - result can change...
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.INFO).
                    description("pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=i,product1.level as PRODUCT1_LEVEL=42").
                    isContained().
               finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.LOW).
                    description("path-info=files-b/included-folder/file-b-1.txt").
                    isContained().
                    finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.LOW).
                    description("path-info=files-b/included-folder/file-b-2.txt").
                    isContained().
                finding().
                    scanType(ScanType.CODE_SCAN).
                    severity(Severity.CRITICAL).
                    description("path-info=files-b/included-folder/subfolder-2/file-b-3.txt").
                    isContained();
        /* @formatter:on */
    }

}
