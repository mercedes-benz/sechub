// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.test.ExampleConstants;

public class AssertUserTest {

	private TestProject project;

	@Before
	public void before() throws Exception {
		project = new TestProject("testProjectId");
	}
	/* ------------------------------------- */
	/* ---------OWNER----------------------- */
	/* ------------------------------------- */

	@Test
	public void checkIsSuperAdmin_user_is_not_superadmin() {
		/* prepare */
		String fetchedUserDetails ="{\"userId\":\"scenario2_owner1\",\"email\":\"scenario2_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[],\"ownedProjects\":[\"scenario2_project1\",\"scenario2_project2\"]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.checkIsSuperAdmin(fetchedUserDetails));

	}

	@Test
	public void checkIsSuperAdmin_user_is_superadmin() {
		/* prepare */
		String fetchedUserDetails ="{\"userId\":\"scenario2_owner1\",\"email\":\"scenario2_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":true,\"projects\":[],\"ownedProjects\":[\"scenario2_project1\",\"scenario2_project2\"]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.checkIsSuperAdmin(fetchedUserDetails));

	}

	/* ------------------------------------- */
	/* ---------ASSIGNED-------------------- */
	/* ------------------------------------- */
	@Test
	public void checkIsAssignedToProject_user_is_not_owner_and_not_user() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"scenario1_projectOther2\"],\"ownedProjects\":[\"scenario1_projectOther1\"]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.checkIsAssignedToProject(project, fetchedUserDetails));

	}

	@Test
	public void checkIsAssignedToProject_user_is_owner_but_not_user() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[],\"ownedProjects\":[\"testProjectId\"]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.checkIsAssignedToProject(project, fetchedUserDetails));

	}

	@Test
	public void checkIsAssignedToProject_user_is_user_but_not_owner() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.checkIsAssignedToProject(project, fetchedUserDetails));

	}

	@Test
	public void checkIsAssignedToProject_user_is_user_and__owner() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[\"testProjectId\"]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.checkIsAssignedToProject(project, fetchedUserDetails));

	}

	/* ------------------------------------- */
	/* ---------OWNER----------------------- */
	/* ------------------------------------- */
	@Test
	public void checkIsOwnerOfProject_user_is_not_owner_and_not_user() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"scenario1_projectOther2\"],\"ownedProjects\":[\"scenario1_projectOther1\"]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.checkIsOwnerOfProject(project, fetchedUserDetails));

	}

	@Test
	public void checkIsOwnerOfProject_user_is_owner_but_not_user() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[],\"ownedProjects\":[\"testProjectId\"]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.checkIsOwnerOfProject(project, fetchedUserDetails));

	}

	@Test
	public void checkIsOwnerOfProject_user_is_user_but_not_owner() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.checkIsOwnerOfProject(project, fetchedUserDetails));

	}

	@Test
	public void checkIsOwnerOfProject_user_is_user_and__owner() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[\"testProjectId\"]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.checkIsOwnerOfProject(project, fetchedUserDetails));

	}

}
