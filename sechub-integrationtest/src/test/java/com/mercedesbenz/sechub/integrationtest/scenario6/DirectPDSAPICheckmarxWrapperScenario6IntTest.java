// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario6;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.test.TestConstants.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.test.TestFileReader;

/**
 * Integration test directly using REST API of integration test PDS (means
 * without sechub).
 *
 * @author Albert Tregnaghi
 *
 */
public class DirectPDSAPICheckmarxWrapperScenario6IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario6.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void pds_techuser_can_start_checkmarx_scan_with_job_configuration_and_PDS_job_is_done() {
        /* @formatter:off */
        /* prepare */
        TestUser user = PDS_TECH_USER;

        String json =TestFileReader.readTextFromFile("src/test/resources/pds/checkmarx-wrapper/checkmarx-pds-job1.json");

        String createResult = asPDSUser(PDS_ADMIN).
                createJobByJsonConfiguration(json);

        UUID pdsJobUUID = assertPDSJobCreateResult(createResult).
                hasJobUUID().
                getJobUUID();

        asPDSUser(user).
            upload(pdsJobUUID, SOURCECODE_ZIP, "pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical.zip");


        /* execute */
        asPDSUser(user).
            markJobAsReadyToStart(pdsJobUUID);

        /* test */
        // we just wait until PDS has been DONE (Means it has not failed)
        TestAPI.waitForPDSJobInState(PDSJobStatusState.DONE, 5, 300, pdsJobUUID, true);
        /* @formatter:on */
    }

}
