// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.IntegrationTestJSONLocation.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

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
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class FullScanDataScenario3SecHubClientIntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(300); // 5 minutes is more than enough...

    @Rule
    public ExpectedException expected =ExpectedExceptionFactory.none();

    @Test
    public void product_failure_results_in_downloadable_fullscan_product_result_is_empty_and_report_contains_vulnerability_1_about_sechub_failure()
            throws IOException {
        /* check preconditions */
        assertUser(USER_1).isAssignedToProject(PROJECT_1).hasOwnerRole().hasUserRole();

        as(SUPER_ADMIN).updateWhiteListForProject(PROJECT_1, Collections.singletonList("https://netsparker.productfailure.demo.example.org"));

        /* prepare - just execute a job */
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, JSON_WEBSCAN_SCENARIO3_PRODUCTFAILURE);
        UUID sechubJobUUID = result.getSechubJobUUD();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute */
        AssertFullScanData assertFullScanData = as(SUPER_ADMIN).downloadFullScanDataFor(sechubJobUUID);

        /* test @formatter:off*/
		assertFullScanData.
		    dumpDownloadFilePath().
		    containsFile("NETSPARKER.txt").// txt because just empty text
		    containsFile("metadata_NETSPARKER.json").// txt because just empty text
		    containsFile("SERECO.json").
			containsFile("metadata_SERECO.json").
		    containsFiles(5);

		FullScanDataElement netsparker = assertFullScanData.resolveFile("NETSPARKER.txt");
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
        UUID sechubJobUUID = result.getSechubJobUUD();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute */
        AssertFullScanData assertFullScanData = as(SUPER_ADMIN).downloadFullScanDataFor(sechubJobUUID);

        /* test @formatter:off*/
        assertFullScanData.
            dumpDownloadFilePath().
            containsFile("CHECKMARX.xml").
            containsFile("metadata_CHECKMARX.json").
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
        UUID sechubJobUUID = result.getSechubJobUUD();

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
        UUID sechubJobUUID = result.getSechubJobUUD();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute */
        expectHttpFailure(() -> as(USER_1).getScanLogsForProject(PROJECT_1), HttpStatus.FORBIDDEN);
    }

    @Test
    public void when_user1_has_started_job_for_project_user1_is_NOT_able_to_download_fullscan_zipfile() throws IOException {
        /* prepare - just execute a job */
        ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN);
        UUID sechubJobUUID = result.getSechubJobUUD();

        assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);

        /* execute */
        expectHttpFailure(() -> as(USER_1).downloadFullScanDataFor(sechubJobUUID), HttpStatus.FORBIDDEN);
    }

}
