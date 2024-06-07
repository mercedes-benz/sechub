// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario3;

import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestJSONLocation.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.integrationtest.api.AssertFullScanData;
import com.mercedesbenz.sechub.integrationtest.api.AssertFullScanData.FullScanDataElement;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultExecutorConfigurations;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class FullScanDataScenario3SecHubClientIntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(300); // 5 minutes is more than enough...

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    /**
     * product failure results in downloadable fullscan product result is empty and
     * report contains vulnerability 1 about sechub failure
     */
    @Test
    public void product_failure_results_in_downloadable_scan_log() throws IOException {
        /* check preconditions */
        UUID netsparkerConfigUUID = ensureExecutorConfigUUID(IntegrationTestDefaultExecutorConfigurations.NETSPARKER_V1);

        assertUser(USER_1).isAssignedToProject(PROJECT_1).hasOwnerRole().hasUserRole();

        as(SUPER_ADMIN).updateWhiteListForProject(PROJECT_1, Collections.singletonList("https://netsparker.productfailure.demo.example.org"));

        /* prepare - just execute a job */
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, CLIENT_JSON_WEBSCAN_PRODUCTFAILURE_ZERO_WAIT);
        UUID sechubJobUUID = result.getSechubJobUUID();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute */
        File scanDataZipFile = as(SUPER_ADMIN).downloadFullScanDataFor(sechubJobUUID);

        /* test @formatter:off*/
        AssertFullScanData assertFullScanData = assertFullScanDataZipFile(scanDataZipFile);

        String netsparkerFileName = "NETSPARKER_"+netsparkerConfigUUID+".txt"; //.txt because just empty text for failed parts
        assertFullScanData.
		    dumpDownloadFilePath().
		    containsFile(netsparkerFileName).
		    containsFile("metadata_NETSPARKER_"+netsparkerConfigUUID+".json").
		    containsFile("SERECO.json").
			containsFile("metadata_SERECO.json").
		    containsFiles(5);

		FullScanDataElement netsparker = assertFullScanData.resolveFile(netsparkerFileName);
		assertEquals("", netsparker.content);
		FullScanDataElement sereco = assertFullScanData.resolveFile("SERECO.json");

		assertTrue(sereco.content.contains("\"type\":\"INTERNAL_ERROR_PRODUCT_FAILED\""));
		/* @formatter:on*/
    }

    @Test
    public void user_1_starts_job_but_only_admin_can_download_scanlog_or_fullscan_data() throws IOException {
        UUID checkmarxConfigUUID = ensureExecutorConfigUUID(IntegrationTestDefaultExecutorConfigurations.NETSPARKER_V1);

        /* prepare - just execute a job */
        TestUser user = USER_1;
        TestProject project = PROJECT_1;
        ExecutionResult result = as(user).withSecHubClient().startSynchronScanFor(project, CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT);
        UUID sechubJobUUID = result.getSechubJobUUID();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute (1) - admin can download scan logs */
        String json = as(SUPER_ADMIN).getScanLogsForProject(project);

        /* test */
        assertNotNull(json);
        assertTrue(json.contains(sechubJobUUID.toString()));
        assertTrue(json.contains(user.getUserId()));

        /* execute (2) - admin can download full scan data */
        File scanDataZipFile = as(SUPER_ADMIN).downloadFullScanDataFor(sechubJobUUID);

        /* execute */
        AssertFullScanData assertFullScanData = assertFullScanDataZipFile(scanDataZipFile);

        /* test @formatter:off*/
        assertFullScanData.
            dumpDownloadFilePath().
            containsFile("CHECKMARX_"+checkmarxConfigUUID+".xml").
            containsFile("metadata_CHECKMARX_" +checkmarxConfigUUID+".json").
            containsFile("metadata_SERECO.json").
            containsFile("SERECO.json").
            containsFiles(5);

        FullScanDataElement log = assertFullScanData.resolveFileStartingWith("log_");
        assertTrue(log.content.contains("executedBy=" + user.getUserId()));
        assertTrue(log.content.contains("projectId=" + project.getProjectId()));


        /* execute (3) + test - user cannot donload logs or full scan data*/
        expectHttpFailure(() -> as(user).getScanLogsForProject(project), HttpStatus.FORBIDDEN);
        expectHttpFailure(() -> as(user).downloadFullScanDataFor(sechubJobUUID), HttpStatus.FORBIDDEN);

        /* execute */
    }

}
