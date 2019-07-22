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
	public void isSuperAdmin_user_is_not_superadmin() {
		/* prepare */
		String fetchedUserDetails ="{\"userId\":\"scenario2_owner1\",\"email\":\"scenario2_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[],\"ownedProjects\":[\"scenario2_project1\",\"scenario2_project2\"]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.isSuperAdmin(fetchedUserDetails));

	}

	@Test
	public void isSuperAdmin_user_is_superadmin() {
		/* prepare */
		String fetchedUserDetails ="{\"userId\":\"scenario2_owner1\",\"email\":\"scenario2_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":true,\"projects\":[],\"ownedProjects\":[\"scenario2_project1\",\"scenario2_project2\"]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.isSuperAdmin(fetchedUserDetails));

	}

	/* ------------------------------------- */
	/* ---------ASSIGNED-------------------- */
	/* ------------------------------------- */
	@Test
	public void isAssignedToProject_user_is_not_owner_and_not_user() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"scenario1_projectOther2\"],\"ownedProjects\":[\"scenario1_projectOther1\"]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.isAssignedToProject(project, fetchedUserDetails));

	}

	@Test
	public void isAssignedToProject_user_is_owner_but_not_user() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[],\"ownedProjects\":[\"testProjectId\"]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.isAssignedToProject(project, fetchedUserDetails));

	}

	@Test
	public void isAssignedToProject_user_is_user_but_not_owner() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.isAssignedToProject(project, fetchedUserDetails));

	}

	@Test
	public void isAssignedToProject_user_is_user_and__owner() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[\"testProjectId\"]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.isAssignedToProject(project, fetchedUserDetails));

	}

	/* ------------------------------------- */
	/* ---------OWNER----------------------- */
	/* ------------------------------------- */
	@Test
	public void isOwnerOfProject_user_is_not_owner_and_not_user() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"scenario1_projectOther2\"],\"ownedProjects\":[\"scenario1_projectOther1\"]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.isOwnerOfProject(project, fetchedUserDetails));

	}

	@Test
	public void isOwnerOfProject_user_is_owner_but_not_user() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[],\"ownedProjects\":[\"testProjectId\"]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.isOwnerOfProject(project, fetchedUserDetails));

	}

	@Test
	public void isOwnerOfProject_user_is_user_but_not_owner() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[]}";

		/* execute */
		/* test */
		assertFalse(AssertUser.isOwnerOfProject(project, fetchedUserDetails));

	}

	@Test
	public void isOwnerOfProject_user_is_user_and__owner() {
		/* prepare */
		String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@"+ExampleConstants.URI_TARGET_SERVER+"\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[\"testProjectId\"]}";

		/* execute */
		/* test */
		assertTrue(AssertUser.isOwnerOfProject(project, fetchedUserDetails));

	}

}
