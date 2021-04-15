// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.IntegrationTestJSONLocation.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
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

import com.daimler.sechub.integrationtest.api.AssertFullScanData;
import com.daimler.sechub.integrationtest.api.AssertFullScanData.FullScanDataElement;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.internal.IntegrationTestDefaultExecutorConfigurations;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

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
        assertUser(USER_1).isAssignedToProject(PROJECT_1).hasOwnerRole().hasUserRole();

        as(SUPER_ADMIN).updateWhiteListForProject(PROJECT_1, Collections.singletonList("https://netsparker.productfailure.demo.example.org"));

        /* prepare - just execute a job */
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, JSON_WEBSCAN_SCENARIO3_PRODUCTFAILURE);
        UUID sechubJobUUID = result.getSechubJobUUID();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute */
        File scanDataZipFile = as(SUPER_ADMIN).downloadFullScanDataFor(sechubJobUUID);

        /* test @formatter:off*/
        AssertFullScanData assertFullScanData = assertFullScanDataZipFile(scanDataZipFile);
      
        String netsparkerFileName = "NETSPARKER_"+IntegrationTestDefaultExecutorConfigurations.NETSPARKER_V1.uuid+".txt"; //.txt because just empty text for failed parts
        assertFullScanData.
		    dumpDownloadFilePath().
		    containsFile(netsparkerFileName).
		    containsFile("metadata_NETSPARKER_"+IntegrationTestDefaultExecutorConfigurations.NETSPARKER_V1.uuid+".json").
		    containsFile("SERECO.json").
			containsFile("metadata_SERECO.json").
		    containsFiles(5);

		FullScanDataElement netsparker = assertFullScanData.resolveFile(netsparkerFileName);
		assertEquals("", netsparker.content);
		FullScanDataElement sereco = assertFullScanData.resolveFile("SERECO.json");

		assertTrue(sereco.content.contains("\"type\":\"SecHub failure\""));
		assertTrue(sereco.content.contains("Security product 'NETSPARKER' failed"));
		/* @formatter:on*/
    }

    @Test
    public void when_job_was_executed__admin_is_able_to_download_fullscan_zip_file_for_this_sechub_job() throws IOException {
        /* check preconditions */
        assertUser(USER_1).isAssignedToProject(PROJECT_1).hasOwnerRole().hasUserRole();

        /* prepare - just execute a job */
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN);
        UUID sechubJobUUID = result.getSechubJobUUID();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        File scanDataZipFile = as(SUPER_ADMIN).downloadFullScanDataFor(sechubJobUUID);

        /* execute */
        AssertFullScanData assertFullScanData = assertFullScanDataZipFile(scanDataZipFile);

        /* test @formatter:off*/
        assertFullScanData.
            dumpDownloadFilePath().
            containsFile("CHECKMARX_"+IntegrationTestDefaultExecutorConfigurations.CHECKMARX_V1.uuid+".xml").
            containsFile("metadata_CHECKMARX_" +IntegrationTestDefaultExecutorConfigurations.CHECKMARX_V1.uuid+".json").
            containsFile("metadata_SERECO.json").
            containsFile("SERECO.json").
            containsFiles(5);

        FullScanDataElement log = assertFullScanData.resolveFileStartingWith("log_");
        assertTrue(log.content.contains("executedBy=" + USER_1.getUserId()));
        assertTrue(log.content.contains("projectId=" + PROJECT_1.getProjectId()));
        /* @formatter:on*/
    }

    @Test
    public void when_user1_has_started_job_for_project_admin_is_able_to_fetch_json_scanlog_which_is_containing_jobuuid_and_executor() throws IOException {
        /* prepare - just execute a job */
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN);
        UUID sechubJobUUID = result.getSechubJobUUID();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute */
        String json = as(SUPER_ADMIN).getScanLogsForProject(PROJECT_1);

        /* test */
        assertNotNull(json);
        assertTrue(json.contains(sechubJobUUID.toString()));
        assertTrue(json.contains(USER_1.getUserId()));
    }

    @Test
    public void when_user1_has_started_job_for_project_user1_is_NOT_able_to_fetch_json_scanlog() throws IOException {
        /* prepare - just execute a job */
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN);
        UUID sechubJobUUID = result.getSechubJobUUID();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute */
        expectHttpFailure(() -> as(USER_1).getScanLogsForProject(PROJECT_1), HttpStatus.FORBIDDEN);
    }

    @Test
    public void when_user1_has_started_job_for_project_user1_is_NOT_able_to_download_fullscan_zipfile() throws IOException {
        /* prepare - just execute a job */
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN);
        UUID sechubJobUUID = result.getSechubJobUUID();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute */
        expectHttpFailure(() -> as(USER_1).downloadFullScanDataFor(sechubJobUUID), HttpStatus.FORBIDDEN);
    }

}
