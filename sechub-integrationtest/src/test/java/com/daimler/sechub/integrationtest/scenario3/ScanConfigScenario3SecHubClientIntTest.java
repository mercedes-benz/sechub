// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario3;

import static com.daimler.sechub.integrationtest.api.IntegrationTestJSONLocation.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario3.Scenario3.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;

public class ScanConfigScenario3SecHubClientIntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario3.class);

	@Rule
	public Timeout timeOut = Timeout.seconds(300); // 5 minutes is more than enough...

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Test
	public void when_scanconfig1_defines_team3_and_preset1_for_project_pattern__project_will_be_scanned_with_this_setup()
			throws IOException {

		/* prepare*/
		String json = IntegrationTestFileSupport.getTestfileSupport().loadTestFile("scan-config/sechub_scan_config1.json");
		changeScanConfig(json);
		clearMetaDataInspection();

		assertInspections().hasAmountOfInspections(0);

		/* execute */
		ExecutionResult result = as(USER_1).withSecHubClient().
				startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN);
		UUID sechubJobUUID = result.getSechubJobUUD();

		/* test */
		/* @formatter:off */
		assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);
		assertInspections().
			hasAmountOfInspections(1).
			inspectionNr(0).
				hasId("CHECKMARX").
				hasNotice("presetid","200001"). // scenario3_project1 -> preset 1
				hasNotice("teamid", "teamid3"); // scenario3_project1 -> team 3
		/* @formatter:on */
	}

	@Test
	public void when_scanconfig2_defines_not_explicit_but_for_allothers_team4_and_preset2_for_projects_with_abc__project1_will_be_scanned_with_this_setup()
			throws IOException {

		/* prepare*/
		String json = IntegrationTestFileSupport.getTestfileSupport().loadTestFile("scan-config/sechub_scan_config2.json");
		changeScanConfig(json);
		clearMetaDataInspection();

		assertInspections().hasAmountOfInspections(0);

		/* execute */
		ExecutionResult result = as(USER_1).withSecHubClient().
				startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN);
		UUID sechubJobUUID = result.getSechubJobUUD();

		/* test */
		/* @formatter:off */
		assertNotNull("No sechub jobUUId found-maybe client call failed?", sechubJobUUID);
		assertInspections().
			hasAmountOfInspections(1).
			inspectionNr(0).
				hasId("CHECKMARX").
				hasNotice("presetid","200002").// scenario3_project1 -> preset 1
				hasNotice("teamid", "teamid4");// scenario3_project1 -> preset 1
		/* @formatter:on */
	}


}
