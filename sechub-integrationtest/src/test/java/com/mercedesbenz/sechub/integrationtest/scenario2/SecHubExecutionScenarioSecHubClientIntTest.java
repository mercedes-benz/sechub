// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

import static com.mercedesbenz.sechub.integrationtest.api.AssertExecutionResult.*;
import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestJSONLocation.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2.*;
import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestJSONLocation;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;

public class SecHubExecutionScenarioSecHubClientIntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(60 * 5);

    @Test
    public void sechub_client_is_able_to_trigger_sourcescan_asynchronous_even_when_userid_is_uppercased() {
        /* @formatter:off */

		/* prepare */
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1);

		assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1);

		/* execute + test */
		as(USER_1.clonedButWithUpperCasedId()).
			withSecHubClient().
				startAsynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT).
				assertJobTriggered();

		/* @formatter:on */

    }

    @Test
    public void sechub_client_is_able_to_trigger_infrascan_asynchronous() {
        /* @formatter:off */

		/* prepare */
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1).
			updateWhiteListForProject(PROJECT_1, asList(IntegrationTestExampleConstants.INFRASCAN_DEFAULT_WHITELEIST_ENTRY));

		assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1);

		/* execute + test */
		as(USER_1).
			withSecHubClient().
			startAsynchronScanFor(PROJECT_1, CLIENT_JSON_INFRASCAN).
			assertJobTriggered();

		/* @formatter:on */

    }

    @SuppressWarnings("deprecation") // we use startDownloadJobReport here - old implementation okay here
    @Test
    public void a_project_having_no_white_list_entries_but_no_problems_can_be_executed_as_codescan_and_results_green() {
        /* @formatter:off */

		/* prepare */
		TestProject project = PROJECT_3;
		TestUser user = USER_1;

		assertProject(project).hasNoWhiteListEntries();

		as(SUPER_ADMIN).
			assignUserToProject(user, project);

		/* execute */
		IntegrationTestJSONLocation location = CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT;
		UUID jobUUID =
	    as(user).
			withSecHubClient().
			startAsynchronScanFor(project, location).
			assertFileUploaded(project).
			assertJobTriggered().
			getJobUUID();

		waitForJobDoneAndFailWhenJobIsFailing(project, jobUUID);

		as(user).
			withSecHubClient().
			startDownloadJobReport(project, jobUUID, location).
			hasStatus(SecHubStatus.SUCCESS).
			hasTrafficLight(TrafficLight.GREEN)

			;
		/* @formatter:on */
    }

    @SuppressWarnings("deprecation") // we use startDownloadJobReport here - old implementation okay here
    @Test
    public void a_project_having_no_metadata_but_no_problems_can_be_executed_as_codescan_and_results_green() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_3;
        TestUser user = USER_1;

        assertProject(project).hasNoMetaData();

        as(SUPER_ADMIN).
            assignUserToProject(user, project);

        /* execute */
        IntegrationTestJSONLocation location = CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT;
        UUID jobUUID =
        as(user).
            withSecHubClient().
            startAsynchronScanFor(project, location).
            assertFileUploaded(project).
            assertJobTriggered().
            getJobUUID();

        waitForJobDoneAndFailWhenJobIsFailing(project, jobUUID);

        as(user).
            withSecHubClient().
            startDownloadJobReport(project, jobUUID, location).
            hasTrafficLight(TrafficLight.GREEN)

            ;
        /* @formatter:on */
    }

    @SuppressWarnings("deprecation") // we use startDownloadJobReport here - old implementation okay here
    @Test
    public void a_project_having_metadata_no_problems_can_be_executed_as_codescan_and_results_green() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_3;
        TestUser user = USER_1;

        assertProject(project).hasNoMetaData();

        Map<String, String> metaData = new HashMap<>();
        metaData.put("key1", "value1");

        as(SUPER_ADMIN).
            assignUserToProject(user, project).
            updateMetaDataForProject(project, metaData);

        assertProject(project).hasMetaData(metaData);

        /* execute */
        IntegrationTestJSONLocation location = CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT;
        UUID jobUUID =
        as(user).
            withSecHubClient().
            startAsynchronScanFor(project, location).
            assertFileUploaded(project).
            assertJobTriggered().
            getJobUUID();

        waitForJobDoneAndFailWhenJobIsFailing(project, jobUUID);

        as(user).
            withSecHubClient().
            startDownloadJobReport(project, jobUUID, location).
            hasTrafficLight(TrafficLight.GREEN);

        /* store webscan reports as example */
        String jsonReport = as(user).getJobReport(project, jobUUID);
        storeTestReport("report_client-test-1-codescan-green.json", jsonReport);

        String htmlReport = as(user).getHTMLJobReport(project, jobUUID);
        storeTestReport("report_client-test-1-codescan-green.html", htmlReport);
        /* @formatter:on */
    }

    @SuppressWarnings("deprecation") // we use startDownloadJobReport here - old implementation okay here
    @Test
    public void a_project_having_metadata_updated_no_problems_can_be_executed_as_codescan_and_results_green() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_3;
        TestUser user = USER_1;

        assertProject(project).hasNoMetaData();

        Map<String, String> metaData = new HashMap<>();
        metaData.put("key1", "value1");

        // add first metaDataEntry
        as(SUPER_ADMIN).
            assignUserToProject(user, project).
            updateMetaDataForProject(project, metaData);

        assertProject(project).hasMetaData(metaData);

        // add additional entry
        metaData.put("key2", "value2");
        as(SUPER_ADMIN).
            updateMetaDataForProject(project, metaData);

        assertProject(project).hasMetaData(metaData);

        // update one entry
        metaData.put("key1", "updatedValue");

        as(SUPER_ADMIN).
            updateMetaDataForProject(project, metaData);

        assertProject(project).hasMetaData(metaData);

        // remove all entries
        metaData.clear();
        as(SUPER_ADMIN).
            updateMetaDataForProject(project, metaData);

        assertProject(project).hasNoMetaData();

        /* execute */
        IntegrationTestJSONLocation location = CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT;
        UUID jobUUID =
        as(user).
            withSecHubClient().
            startAsynchronScanFor(project, location).
            assertFileUploaded(project).
            assertJobTriggered().
            getJobUUID();

        waitForJobDoneAndFailWhenJobIsFailing(project, jobUUID);

        as(user).
            withSecHubClient().
            startDownloadJobReport(project, jobUUID, location).
            hasTrafficLight(TrafficLight.GREEN)

            ;
        /* @formatter:on */
    }

    @SuppressWarnings("deprecation") // we use startDownloadJobReport here - old implementation okay here
    @Test
    public void a_project_having_no_problems_can_be_executed_as_codescan_and_results_green() {
        /* @formatter:off */

		/* prepare */
		TestProject project = PROJECT_3;
		TestUser user = USER_1;

		assertProject(project).hasNoWhiteListEntries();

		List<String> list = new ArrayList<>();
		as(SUPER_ADMIN).
			updateWhiteListForProject(project, list).
			assignUserToProject(user, project);

		/* execute */
		IntegrationTestJSONLocation location = CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT;
		UUID jobUUID =
	    as(user).
			withSecHubClient().
			startAsynchronScanFor(project, location).
			assertFileUploaded(project).
			assertJobTriggered().
			getJobUUID();

		waitForJobDoneAndFailWhenJobIsFailing(project, jobUUID);

		as(user).
			withSecHubClient().
			startDownloadJobReport(project, jobUUID, location).
			hasTrafficLight(TrafficLight.GREEN)

			;
		/* @formatter:on */

    }

    @SuppressWarnings("deprecation") // we use startDownloadJobReport here - old implementation okay here
    @Test
    public void sechub_client_can_execute_a_config_file_which_uses_template_variables_of_environment_entries_but_no_data_section() {
        /* @formatter:off */

		/* prepare */
		TestProject project = PROJECT_3;
		TestUser user = USER_1;

		assertProject(project).hasNoWhiteListEntries();

		List<String> list = new ArrayList<>();
		as(SUPER_ADMIN).
			updateWhiteListForProject(project, list).
			assignUserToProject(user, project);

		Map<String, String> envEntries = new LinkedHashMap<>();
		envEntries.put("SHTEST_VERSION", "1.0");
		envEntries.put("SHTEST_FOLDERS1", IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__1_SECOND_WAITING.getMockDataIdentifier());

		/* execute */
		IntegrationTestJSONLocation location = IntegrationTestJSONLocation.CLIENT_JSON_SOURCESCAN_GENERIC_TEMPLATE_NO_DATA_SECTION;
		UUID jobUUID =
	    as(user).
			withSecHubClient().
			startAsynchronScanFor(project, location, envEntries).
			assertFileUploaded(project).
			assertJobTriggered().
			getJobUUID();

		waitForJobDoneAndFailWhenJobIsFailing(project, jobUUID);

		/* why test green result ? Because we set test folders in a way we
		 * will expect green traffic light - which is only the case when
		 * we have an explicit path set by the environment entry inside
		 * template... We could also rely on server validation of version
		 * but this way is better, because we rely on test environment /mocked
		 * adapter behavior which is well known.
		 */
		as(user).
			withSecHubClient().
			startDownloadJobReport(project, jobUUID, location).
			hasTrafficLight(TrafficLight.GREEN)

			;
		/* @formatter:on */

    }

    @SuppressWarnings("deprecation") // we use startDownloadJobReport here - old implementation okay here
    @Test
    public void a_project_having_no_white_list_entries_but_some_problems_can_be_executed_as_codescan_and_results_yellow() {
        /* @formatter:off */

		/* prepare */
		TestProject project = PROJECT_3;
		TestUser user = USER_1;

		assertProject(project).hasNoWhiteListEntries();

		as(SUPER_ADMIN).
			assignUserToProject(user, project);


		/* execute */
		IntegrationTestJSONLocation location = CLIENT_JSON_SOURCESCAN_YELLOW_ZERO_WAIT;
		UUID jobUUID =
	    as(user).
			withSecHubClient().
			startAsynchronScanFor(project, location).
			assertFileUploaded(project).
			assertJobTriggered().
			getJobUUID();

		waitForJobDoneAndFailWhenJobIsFailing(project, jobUUID);

		as(user).
			withSecHubClient().
			startDownloadJobReport(project, jobUUID, location).
			hasTrafficLight(TrafficLight.YELLOW)

			;
		/* @formatter:on */

    }

    @Test
    public void sechub_client_is_able_to_handle_asynchronous_and_file_is_uploaded() {
        /* @formatter:off */

		/* prepare */
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1);

		assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1);

		/* execute */
		as(USER_1).
			withSecHubClient().
			startAsynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT).
			assertFileUploaded(PROJECT_1);

		/* @formatter:on */

    }

    @Test
    public void sechub_client_is_able_to_handle_asynchronous_and_file_is_uploaded_and_excludes_handled() {
        /* @formatter:off */

		/* prepare */
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1);

		assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1);

		/* Need to ignore default excludes, because "test" is in the directory tree. See Issue #754 */
		Map<String, String> environmentVariables = new HashMap<>();
		environmentVariables.put("SECHUB_IGNORE_DEFAULT_EXCLUDES", "true");

		/* execute */

		as(USER_1).
			withSecHubClient().
			// `sechub-integrationtest-client-sourcescan-excluded_some_files.json`
			// uses a mock with 5 seconds running job - enough to get access to
			// the uploaded content, download it full. Otherwise file could
			// be automated removed by cleanup actions on server!
			startAsynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_EXLUDE_SOME_FILES, environmentVariables).
				assertFileUploadedAsZip(PROJECT_1).
					zipContains("sechub-integrationtest/src/test/resources/checksum-testfiles/not-excluded.txt").
					zipContains("sechub-integrationtest/src/test/resources/checksum-testfiles/subfolder/not-excluded-2.txt").
					zipNotContains("sechub-integrationtest/src/test/resources/checksum-testfiles/exclude-me.txt").
					zipNotContains("sechub-integrationtest/src/test/resources/checksum-testfiles/subfolder/exclude-me.txt");

		/* @formatter:on */

    }

    @Test
    public void sechub_client_is_able_to_handle_synchronous_and_result_has_trafficlight_green() {

        /* prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        /* @formatter:off */
		assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1);

		/* execute */
		ExecutionResult result = as(USER_1).withSecHubClient().startSynchronScanFor(PROJECT_1,
		        CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT_BIG_CONFIGFILE);

		/* test */
		assertResult(result).
			isGreen().
			hasExitCode(0);

		/* @formatter:on */

    }

    @Test
    public void sechub_client_is_able_to_handle_synchronous_and_result_has_trafficlight_yellow_pe_default_exitcode0() {

        /* prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        /* @formatter:off */
		assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1);

		/* execute */
		ExecutionResult result = as(USER_1).
				withSecHubClient().
				startSynchronScanFor(PROJECT_1, CLIENT_JSON_SOURCESCAN_YELLOW_ZERO_WAIT);

		/* test */
		assertResult(result).
			isYellow().
			hasExitCode(0);

		/* @formatter:on */

    }

    @Test
    public void sechub_client_is_able_to_handle_synchronous_and_result_has_trafficlight_yellow_stop_on_yellow_active_so_exit_code1() {

        /* prepare */
        TestProject project = PROJECT_1;

        as(SUPER_ADMIN).assignUserToProject(USER_1, project);

        /* @formatter:off */
		assertUser(USER_1).
			doesExist().
			isAssignedToProject(project);

		/* execute */
		ExecutionResult result = as(USER_1).
				withSecHubClient().
				enableStopOnYellow().
				startSynchronScanFor(project, CLIENT_JSON_SOURCESCAN_YELLOW_ZERO_WAIT);

		/* test */
		assertResult(result).
			isYellow().
			hasExitCode(1);

		/* store webscan reports as example */
        String jsonReport = as(USER_1).getJobReport(project, result.getSechubJobUUID());
        storeTestReport("report_client-test-2-codescan-yellow.json", jsonReport);

        String htmlReport = as(USER_1).getHTMLJobReport(project, result.getSechubJobUUID());
        storeTestReport("report_client-test-2-codescan-yellow.html", htmlReport);
		/* @formatter:on */

    }

    @Test
    public void sechub_client_is_able_to_handle_synchronous_and_result_has_trafficlight_red() {

        /* @formatter:off */
		/* prepare */
		TestProject project = PROJECT_1;

        as(SUPER_ADMIN).
			assignUserToProject(USER_1, project);

		assertUser(USER_1).
			doesExist().
			isAssignedToProject(project);

		/* execute */
		ExecutionResult result = as(USER_1).
				withSecHubClient().
				startSynchronScanFor(project, CLIENT_JSON_WEBSCAN_RED_ZERO_WAIT);

		/* test */
		assertResult(result).
			isRed().
			hasExitCode(1);

		 /* store webscan reports as example */
        String jsonReport = as(USER_1).getJobReport(project, result.getSechubJobUUID());
        storeTestReport("report_client-test-3-webscan-red-one-finding.json", jsonReport);

        String htmlReport = as(USER_1).getHTMLJobReport(project, result.getSechubJobUUID());
        storeTestReport("report_client-test-3-webscan-red-one-finding.html", htmlReport);

        /* execute 2 - same setup, but result will have no mutiple entries inside (low, medium, high, criticial )*/
        result = as(USER_1).
                withSecHubClient().
                startSynchronScanFor(project, CLIENT_JSON_WEBSCAN_RED_MANYFINDINGS_ZERO_WAIT);

        /* test */
        assertResult(result).   
            isRed().
            hasExitCode(1);


         /* store webscan reports as example */
        String jsonReport2  = as(USER_1).getJobReport(project, result.getSechubJobUUID());
        storeTestReport("report_client-test-4-webscan-red-multiple-findings.json", jsonReport2);

        String htmlReport2 = as(USER_1).getHTMLJobReport(project, result.getSechubJobUUID());
        storeTestReport("report_client-test-4-webscan-red-multiple-findings.html", htmlReport2);
        
        /* test */
        
        /* @formatter:on */

    }

}
