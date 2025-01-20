// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.ExampleConstants;

@TestOnlyForRegularExecution
class AssertUserTest {

    private TestProject project;

    @BeforeEach
    void beforeEach() throws Exception {
        project = new TestProject("testProjectId");
    }
    /* ------------------------------------- */
    /* ---------OWNER----------------------- */
    /* ------------------------------------- */

    @Test
    void checkIsSuperAdmin_user_is_not_superadmin() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario2_owner1\",\"email\":\"scenario2_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":false,\"projects\":[],\"ownedProjects\":[\"scenario2_project1\",\"scenario2_project2\"]}";

        /* execute */
        /* test */
        assertFalse(AssertUser.checkIsSuperAdmin(fetchedUserDetails));

    }

    @Test
    void checkIsSuperAdmin_user_is_superadmin() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario2_owner1\",\"email\":\"scenario2_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":true,\"projects\":[],\"ownedProjects\":[\"scenario2_project1\",\"scenario2_project2\"]}";

        /* execute */
        /* test */
        assertTrue(AssertUser.checkIsSuperAdmin(fetchedUserDetails));

    }

    /* ------------------------------------- */
    /* ---------ASSIGNED-------------------- */
    /* ------------------------------------- */
    @Test
    void checkIsAssignedToProject_user_is_not_owner_and_not_user() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":false,\"projects\":[\"scenario1_projectOther2\"],\"ownedProjects\":[\"scenario1_projectOther1\"]}";

        /* execute */
        /* test */
        assertFalse(AssertUser.checkIsAssignedToProject(project, fetchedUserDetails));

    }

    @Test
    void checkIsAssignedToProject_user_is_owner_but_not_user() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":false,\"projects\":[],\"ownedProjects\":[\"testProjectId\"]}";

        /* execute */
        /* test */
        assertFalse(AssertUser.checkIsAssignedToProject(project, fetchedUserDetails));

    }

    @Test
    void checkIsAssignedToProject_user_is_user_but_not_owner() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[]}";

        /* execute */
        /* test */
        assertTrue(AssertUser.checkIsAssignedToProject(project, fetchedUserDetails));

    }

    @Test
    void checkIsAssignedToProject_user_is_user_and__owner() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[\"testProjectId\"]}";

        /* execute */
        /* test */
        assertTrue(AssertUser.checkIsAssignedToProject(project, fetchedUserDetails));

    }

    /* ------------------------------------- */
    /* ---------OWNER----------------------- */
    /* ------------------------------------- */
    @Test
    void checkIsOwnerOfProject_user_is_not_owner_and_not_user() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":false,\"projects\":[\"scenario1_projectOther2\"],\"ownedProjects\":[\"scenario1_projectOther1\"]}";

        /* execute */
        /* test */
        assertFalse(AssertUser.checkIsOwnerOfProject(project, fetchedUserDetails));

    }

    @Test
    void checkIsOwnerOfProject_user_is_owner_but_not_user() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":false,\"projects\":[],\"ownedProjects\":[\"testProjectId\"]}";

        /* execute */
        /* test */
        assertTrue(AssertUser.checkIsOwnerOfProject(project, fetchedUserDetails));

    }

    @Test
    void checkIsOwnerOfProject_user_is_user_but_not_owner() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[]}";

        /* execute */
        /* test */
        assertFalse(AssertUser.checkIsOwnerOfProject(project, fetchedUserDetails));

    }

    @Test
    void checkIsOwnerOfProject_user_is_user_and__owner() {
        /* prepare */
        String fetchedUserDetails = "{\"userId\":\"scenario1_owner1\",\"email\":\"scenario1_owner1@" + ExampleConstants.URI_TARGET_SERVER
                + "\",\"superAdmin\":false,\"projects\":[\"testProjectId\"],\"ownedProjects\":[\"testProjectId\"]}";

        /* execute */
        /* test */
        assertTrue(AssertUser.checkIsOwnerOfProject(project, fetchedUserDetails));

    }

}
