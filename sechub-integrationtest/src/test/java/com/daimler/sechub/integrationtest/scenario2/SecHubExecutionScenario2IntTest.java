// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.springframework.http.HttpStatus;

import com.daimler.sechub.integrationtest.api.AnonymousTestUser;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.integrationtest.api.TestUser;

public class SecHubExecutionScenario2IntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

	@Rule
	public Timeout timeOut = Timeout.seconds(30);

	/* +-----------------------------------------------------------------------+ */
	/* +............................ Start scan job ...........................+ */
	/* +-----------------------------------------------------------------------+ */
	@Test
	public void when_user_is_not_assigned_to_project_job_cannot_be_started() {
		/* @formatter:off */
		assertUser(USER_1).
			doesExist().
			isNotAssignedToProject(PROJECT_1).
			canNotCreateWebScan(PROJECT_1, HttpStatus.NOT_FOUND);// we use not found because for user1 it is not existent...
		/* @formatter:on */
	}
	
	@Test
	public void admin_is_able_to_start_scan_when_project_exists() {
		/* prepare*/
		/* @formatter:off */

		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1);
		
		assertUser(USER_1).
			isAssignedToProject(PROJECT_1);
		
		/* test */
		assertUser(SUPER_ADMIN)
			.canCreateWebScan(PROJECT_1);
		
		/* @formatter:on */
	}
	
	@Test
	public void admin_is_not_able_to_start_scan_when_project_not_exists() {
		/* prepare*/
		/* @formatter:off */

		expectHttpFailure(() -> TestAPI.as(SUPER_ADMIN).createWebScan(new TestProject("notexistingproject"), false), HttpStatus.NOT_FOUND);
		/* @formatter:on */

	}

	@Test
	public void when_user_is_assigned_to_project_job_can_be_created_and_user_and_superadmin_can_get_status_but_not_other_users() {
		/* prepare*/
		/* @formatter:off */
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1).
			assignUserToProject(USER_2, PROJECT_2);

		assertUser(USER_2).
			isAssignedToProject(PROJECT_2).
			isNotAssignedToProject(PROJECT_1);

		UUID jobUUID = assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1).
			canCreateWebScan(PROJECT_1);

		;
		/* test */
		assertUser(USER_1).
			canGetStatusForJob(PROJECT_1, jobUUID);
		assertUser(SUPER_ADMIN).
			canGetStatusForJob(PROJECT_1, jobUUID);
		assertUser(USER_2).
			canNotGetStatusForJob(PROJECT_1, jobUUID, HttpStatus.NOT_FOUND).
			canNotGetStatusForJob(PROJECT_2, jobUUID, HttpStatus.NOT_FOUND);

		/* @formatter:on */

	}

	@Test
	public void when_user_is_assigned_to_project_job_can_be_approved() {
		/* prepare*/
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1).
			assignUserToProject(USER_2, PROJECT_2);

		/* @formatter:off */
		assertUser(USER_2).
			isAssignedToProject(PROJECT_2).
			isNotAssignedToProject(PROJECT_1);

		UUID jobUUID = assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1).
			canCreateWebScan(PROJECT_1);

		;

		assertUser(USER_2).canNotApproveJob(PROJECT_1, jobUUID);
		assertUser(USER_1).canApproveJob(PROJECT_1, jobUUID);

		/* @formatter:on */

	}

	@Test
	public void an_admin_which_is_not_assigned_to_a_project_can_approve_a_job_triggered_by_another_user() {
		/* prepare*/
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1).
			assignUserToProject(USER_2, PROJECT_2);

		/* @formatter:off */
		assertUser(USER_2).
			isAssignedToProject(PROJECT_2).
			isNotAssignedToProject(PROJECT_1);

		UUID jobUUID = assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1).
			canCreateWebScan(PROJECT_1);

		;

		assertUser(SUPER_ADMIN).canApproveJob(PROJECT_1, jobUUID);

		/* @formatter:on */

	}

	@Test
	public void when_user_is_triggered_a_job_the_report_can_be_downloaded_by_user_superadmins_but_not_other_users() {
		/* prepare*/
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1).
			assignUserToProject(USER_2, PROJECT_2);

		/* @formatter:off */
		assertUser(USER_2).
			isAssignedToProject(PROJECT_2).
			isNotAssignedToProject(PROJECT_1);

		UUID jobUUID = assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1).
			canCreateAndApproveWebScan(PROJECT_1);

		;
		TestAPI.waitForJobDone(PROJECT_1,jobUUID);

		/* test */
		assertUser(USER_1).
			canDownloadReportForJob(PROJECT_1, jobUUID);
		assertUser(SUPER_ADMIN).
			canDownloadReportForJob(PROJECT_1, jobUUID);

		assertUser(USER_2).
			canNotDownloadReportForJob(PROJECT_1, jobUUID);

		/* @formatter:on */

	}


	@Test
	public void when_user_exists_user_cannot_be_signed_in_again() {
		/* @formatter:off */
		assertUser(USER_1).doesExist(); /* already created */
		assertSignup(USER_1).doesNotExist(); // signup is not existing
		TestUser newUser = new AnonymousTestUser(USER_1.getUserId(),"somewhere."+System.currentTimeMillis()+"@example.org");

		/* execute + test */
		expectHttpFailure(()->as(ANONYMOUS).signUpAs(newUser), HttpStatus.NOT_ACCEPTABLE);

		/* @formatter:on */

	}

	@Test
	public void when_another_user_has_got_the_email_used_for_signup_but_different_name_user_cannot_be_signed_in_again() {
		/* @formatter:off */
		assertUser(USER_1).doesExist(); /* already created */
		String name = "u_"+System.currentTimeMillis();
		if (name.length()>15 || name.length()<5) {
			throw new IllegalStateException("testcase corrupt - name invalid:"+name+". Testcase checks only for same email recognized. Name must be correct here!");
		}
		TestUser newUser = new AnonymousTestUser(name,USER_1.getEmail());
		/* execute + test */
		expectHttpFailure(()->as(ANONYMOUS).signUpAs(newUser), HttpStatus.NOT_ACCEPTABLE);

		/* @formatter:on */

	}

	@Test
	public void when_user_is_assigned_to_project_user_can_be_unassigned() {
		as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

		/* @formatter:off */
		assertUser(USER_1).
			isAssignedToProject(PROJECT_1);

		/* execute*/
		as(SUPER_ADMIN).
			unassignUserFromProject(USER_1, PROJECT_1);

		assertUser(USER_1).
			isNotAssignedToProject(PROJECT_1);

		/* @formatter:on */

	}

	@Test
	public void when_admin_updates_whitelist_of_project_to_empty_list_user_is_assigned_to_project_but_job_cannot_be_started__HTTP_STATUS_406_NOT_ACCEPTABLE() {
		/* @formatter:off */

		/* prepare */
		assertProject(PROJECT_1).doesExist();
		as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

		/* execute */
		as(SUPER_ADMIN).
			updateWhiteListForProject(PROJECT_1,Arrays.asList());

		/* test */
		assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1).
			canNotCreateWebScan(PROJECT_1, HttpStatus.NOT_ACCEPTABLE);

		/* @formatter:on */

	}


	@Test
	public void when_user_is_assigned_to_project_job_can_be_created_and_approved_user_1_can_get_report_but_not_user2() {
	    /* @formatter:off */
		as(SUPER_ADMIN).
		    assignUserToProject(USER_1, PROJECT_1).
		    assignUserToProject(USER_2, PROJECT_2);

		assertUser(USER_2).
			isAssignedToProject(PROJECT_2).
			isNotAssignedToProject(PROJECT_1);

		UUID jobUUID = assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1).
			canCreateWebScan(PROJECT_1);

		assertUser(USER_1).
			canApproveJob(PROJECT_1, jobUUID).
			canGetReportForJob(PROJECT_1, jobUUID);
		/* test user 2 has no access to job status or report even when having access to another project!*/
		assertUser(USER_2).
			canNotGetReportForJob(PROJECT_1, jobUUID, HttpStatus.NOT_FOUND).
			canNotGetReportForJob(PROJECT_2, jobUUID, HttpStatus.NOT_FOUND);
		/* @formatter:on */

	}


	@Test
	public void a_zipped_source_file_can_be_uploaded_and_approved() {
		as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

		/* @formatter:off */
		UUID jobUUID = assertUser(USER_1).
			doesExist().
			isAssignedToProject(PROJECT_1).
			canCreateWebScan(PROJECT_1);

		assertUser(USER_1).
			canUploadSourceZipFile(PROJECT_1,jobUUID,"zipfile_contains_only_test1.txt.zip").
			canApproveJob(PROJECT_1, jobUUID).
			canGetReportForJob(PROJECT_1, jobUUID);
		/* @formatter:on */

	}

}
