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

import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;

public class PDSBinaryAndSourceCodeHandlingScenario14IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario14.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    /**
     * Bla
     *
     * <pre>
     *   /__data__
     *      critical-id
     *         data.txt <-- will contain findings
     *         criticial-file.txt <-- only
     * </pre>
     */
    @Test
    public void pds_upload_binaries_extracts_automatically_only_wanted_parts_config_references_criticial_id() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createScanJobWhichUsesDataReferencedIds(IntegrationTestTemplateFile.CODE_SCAN_2_BINARIES_DATA_ONE_REFERENCE, project, NOT_MOCKED, "files-a");

        /* execute */
        as(USER_1).
            uploadBinaries(project, jobUUID, PATH_TO_TARFILE_WITH_DIFFERENT_DATA_SECTIONS).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID,30,true);

        /* test */
        String report = as(USER_1).getJobReport(project, jobUUID);

        assertReport(report).
            hasStatus(SecHubStatus.SUCCESS).
            hasMessages(0);

        assertReport(report).
            hasTrafficLight(YELLOW);
//                finding(0).
//                  hasScanType(ScanType.CODE_SCAN).
//                  hasSeverity(Severity.CRITICAL).
//                  hasDescription("i am a critical error").
//                finding(1).
//                  hasScanType(ScanType.CODE_SCAN).
//                  hasSeverity(Severity.MEDIUM).
//                  hasDescription("i am a medium error").
//                finding(2).
//                  hasScanType(ScanType.CODE_SCAN).
//                  hasSeverity(Severity.INFO).
//                  hasDescription("i am just an information").
//                // here comes dynamic parts:
//                finding(3).
//                  hasScanType(ScanType.CODE_SCAN).
//                  hasSeverity(Severity.INFO).
//                  // we check the parameters in next line: we are using variant a in this scenario, level is always 42, but given as job parameter and returned by integrationtest-codescan.sh
//                  hasDescription("pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=a,product1.level as PRODUCT1_LEVEL="+IntegrationTestDefaultExecutorConfigurations.VALUE_PRODUCT_LEVEL);
        /* @formatter:on */
    }

}
