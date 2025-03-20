// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.test.TestConstants.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.integrationtest.JSONTestSupport;
import com.mercedesbenz.sechub.integrationtest.api.AssertJobScheduler.TestExecutionState;

import junit.framework.AssertionFailedError;

public class AssertUser extends AbstractAssert {

    private TestUser user;
    private String cachedFetchedUserDetails;
    private String cachedFetchedSuperAdminList;

    AssertUser(TestUser user) {
        this.user = user;
    }

    public AssertUser doesNotExist() {
        return doesNotExist(1);
    }

    /**
     * Check user does exists
     *
     * @param tries - amount of retries . Every retry will wait 1 second
     * @return
     */
    public AssertUser doesNotExist(int tries) {
        AssertionFailedError failure = null;
        for (int i = 0; i < tries && failure == null; i++) {
            try {
                if (i > 0) {
                    /* we wait before next check */
                    TestAPI.waitSeconds(1);
                }
                expectHttpClientError(HttpStatus.NOT_FOUND, () -> fetchUserDetailsNotCached(), user.getUserId() + " found!");
            } catch (AssertionFailedError e) {
                failure = e;
            }
        }
        if (failure != null) {
            throw failure;
        }
        return this;
    }

    /**
     * Asserts user does exist
     *
     * @return
     */
    public AssertUser doesExist() {
        fetchUserDetails();// will fail with http error when not available
        return this;

    }

    public AssertUser isNotAssignedToProject(TestProject project) {
        if (internalIsAssignedToProject(project)) {
            fail("User " + user.getUserId() + " is assigned to project " + project.getProjectId());
        }
        return this;
    }

    public AssertUser isAssignedToProject(TestProject project) {
        if (!internalIsAssignedToProject(project)) {
            fail("User " + user.getUserId() + " is NOT assigned to project " + project.getProjectId());
        }
        return this;
    }

    boolean internalIsAssignedToProject(TestProject project) {
        String fetchUserDetails = fetchUserDetails();
        return checkIsAssignedToProject(project, fetchUserDetails);
    }

    static boolean checkIsAssignedToProject(TestProject project, String fetchedUserDetails) {
        return checkIsInList(project, fetchedUserDetails, "projects");
    }

    public AssertUser isSuperAdmin() {
        assertTrue("Is not a super admin!", internalIsSuperAdmin());
        return this;
    }

    public AssertUser isInSuperAdminList() {
        assertTrue("Is not in super admin list!", internalIsInSuperAdminList());
        return this;
    }

    public AssertUser isNotInSuperAdminList() {
        assertFalse("Is in super admin list, but shouldn't!", internalIsInSuperAdminList());
        return this;
    }

    private boolean internalIsInSuperAdminList() {
        String adminList = fetchSuperAdminList();
        return adminList.contains("\"" + user.getUserId() + "\"");
    }

    boolean internalIsSuperAdmin() {
        String fetchUserDetails = fetchUserDetails();
        return checkIsSuperAdmin(fetchUserDetails);
    }

    public AssertUser isNotSuperAdmin() {
        assertFalse("Is a super admin!", internalIsSuperAdmin());
        return this;
    }

    static boolean checkIsSuperAdmin(String fetchedUserDetails) {
        try {
            JsonNode json = JSONTestSupport.DEFAULT.fromJson(fetchedUserDetails);
            JsonNode superAdmin = json.get("superAdmin");
            return superAdmin.asBoolean();
        } catch (IOException e) {
            throw new AssertionError("Was not able to parse json:" + fetchedUserDetails, e);
        }
    }

    static boolean checkIsOwnerOfProject(TestProject project, String fetchedUserDetails) {
        return checkIsInList(project, fetchedUserDetails, "ownedProjects");
    }

    static boolean checkIsInList(TestProject project, String fetchedUserDetails, String listName) {
        try {
            JsonNode json = JSONTestSupport.DEFAULT.fromJson(fetchedUserDetails);
            JsonNode projects = json.get(listName);
            if (!projects.isArray()) {
                fail("not a array found!");
            }
            ArrayNode array = (ArrayNode) projects;
            Iterator<JsonNode> elements = array.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                String text = element.asText();
                if (text.contentEquals(project.getProjectId())) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new AssertionError("Was not able to parse json:" + fetchedUserDetails, e);
        }
    }

    private String fetchUserDetails() {
        if (cachedFetchedUserDetails == null) {
            cachedFetchedUserDetails = fetchUserDetailsNotCached();
        }
        return cachedFetchedUserDetails;
    }

    private String fetchUserDetailsNotCached() {
        return getRestHelper().getJSON(getUrlBuilder().buildGetUserDetailsUrl(user.getUserId()));
    }

    private String fetchSuperAdminList() {
        if (cachedFetchedSuperAdminList == null) {
            cachedFetchedSuperAdminList = fetchSuperAdminListNotCached();
        }
        return cachedFetchedSuperAdminList;
    }

    private String fetchSuperAdminListNotCached() {
        return getRestHelper().getJSON(getUrlBuilder().buildAdminListsAdminsUrl());
    }

    /**
     * Assert user cannot create project - but will also fail if the project exists
     * before
     *
     * @param project
     * @return this
     */
    public AssertUser cannotCreateProject(TestProject project, String owner, HttpStatus errorStatus) {
        if (errorStatus == null) {
            errorStatus = HttpStatus.FORBIDDEN;
        }
        assertProject(project).doesNotExist();

        expectHttpFailure(() -> as(user).createProject(project, owner), errorStatus);

        assertProject(project).doesNotExist();
        return this;
    }

    /**
     * Asserts that the user can create given project. Will fail if the project does
     * exist before, create project not possible or the project does not exist after
     * call. After this is executed the project exists
     *
     * @param project
     * @return this
     */
    public AssertUser canCreateProject(TestProject project, TestUser owner) {
        assertProject(project).doesNotExist();
        as(user).createProject(project, owner);
        assertProject(project).doesExist();

        return this;
    }

    /**
     * Asserts that the user can assign targetUser to given project. Will fail if
     * the project or target user does not exist before, or assignment is not
     * possible.<br>
     * <br>
     * After this is executed the user is assigned to project or test fails
     *
     * @param targetUser
     * @param project
     * @return
     */
    public AssertUser canAssignUserToProject(TestUser targetUser, TestProject project) {
        /* @formatter:off */
		assertProject(project).
			doesExist();
		assertUser(targetUser).
			doesExist().
			isNotAssignedToProject(project);

		as(this.user).
			assignUserToProject(targetUser, project);

		assertUser(targetUser).
			isAssignedToProject(project);
		/* @formatter:on */
        return this;
    }

    /**
     * Asserts that the user can NOT assign targetUser to given project. Will fail
     * if the project or target user does not exist before, or assignment is was
     * possible.<br>
     * <br>
     * After this is executed the user is NOT assigned to project or test fails
     *
     * @param targetUser
     * @param project
     * @return
     */
    public AssertUser canNotAssignUserToProject(TestUser targetUser, TestProject project, HttpStatus expectedError) {
        if (expectedError == null) {
            expectedError = HttpStatus.FORBIDDEN;
        }
        assertProject(project).doesExist();
        assertUser(targetUser).doesExist().isNotAssignedToProject(project);

        expectHttpFailure(() -> as(user).assignUserToProject(targetUser, project), expectedError);

        assertUser(targetUser).isNotAssignedToProject(project);
        return this;
    }

    public AssertUser canAccessProjectInfo(TestProject project) {
        assertProject(project).doesExist();
        accessProjectInfo(project);
        return this;
    }

    private void accessProjectInfo(TestProject project) {
        as(user).getStringFromURL(getUrlBuilder().buildAdminFetchProjectInfoUrl(project.getProjectId()));
    }

    public AssertUser canNotListProject(TestProject project, HttpStatus expectedError) {
        if (expectedError == null) {
            expectedError = HttpStatus.FORBIDDEN;
        }
        assertProject(project).doesExist();

        expectHttpFailure(() -> accessProjectInfo(project), expectedError);

        return this;

    }

    /**
     * Creates a webscan job for project (but job is not started)
     *
     * @param project
     * @return uuid for created job
     */
    public UUID canCreateWebScan(TestProject project) {
        return canCreateWebScan(project, null);
    }

    /**
     * Creates a webscan job for project (but job is not started)
     *
     * @param project
     * @return uuid for created job
     */
    public UUID canCreateAndApproveWebScan(TestProject project) {
        UUID jobUUID = canCreateWebScan(project, null);
        assertNotNull(jobUUID);
        canApproveJob(project, jobUUID);
        return jobUUID;
    }

    /**
     * Creates a webscan job for project (but job is not started)
     *
     * @param project
     * @param runModem mode to use
     * @return uuid for created job
     */
    public UUID canCreateWebScan(TestProject project, IntegrationTestMockMode runMode) {
        return TestAPI.as(user).createWebScan(project, runMode, true);
    }

    public AssertUser canNotCreateWebScan(TestProject project, HttpStatus expectedError) {
        if (expectedError == null) {
            expectedError = HttpStatus.FORBIDDEN;
        }
        assertProject(project).doesExist();

        expectHttpFailure(() -> canCreateWebScan(project), expectedError);
        return this;
    }

    public AssertUser canGetStatusForJob(TestProject project, UUID jobUUID) {
        as(user).getJobStatus(project.getProjectId(), jobUUID);
        return this;

    }

    public AssertUser canApproveJob(TestProject project, UUID jobUUID) {
        as(user).approveJob(project, jobUUID);
        return this;
    }

    public AssertUser afterThis() {
        // just syntax sugar for more readable tests
        return this;
    }

    public AssertUser now() {
        // just syntax sugar for more readable tests
        return this;
    }

    public AssertUser canNotApproveJob(TestProject project, UUID jobUUID) {
        expectHttpFailure(() -> canApproveJob(project, jobUUID), HttpStatus.NOT_FOUND);
        return this;
    }

    public AssertUser canNotGetStatusForJob(TestProject project, UUID jobUUID, HttpStatus expectedError) {
        if (expectedError == null) {
            expectedError = HttpStatus.FORBIDDEN;
        }
        assertProject(project).doesExist();

        expectHttpFailure(() -> canGetStatusForJob(project, jobUUID), expectedError);
        return this;
    }

    public AssertUser canGetReportForJob(TestProject project, UUID jobUUID) {
        as(user).getJobReport(project.getProjectId(), jobUUID);
        return this;

    }

    public AssertUser canLogin() {
        as(user).getServerURL();
        return this;
    }

    public AssertUser canNotGetReportForJob(TestProject project, UUID jobUUID, HttpStatus expectedError) {
        if (expectedError == null) {
            expectedError = HttpStatus.FORBIDDEN;
        }
        assertProject(project).doesExist();

        expectHttpFailure(() -> canGetReportForJob(project, jobUUID), expectedError);
        return this;
    }

    public AssertUser canUploadSourceZipFile(TestProject project, UUID jobUUID, String pathInsideResources) {
        as(user).uploadSourcecode(project, jobUUID, pathInsideResources);
        /* check if file is uploaded on server location */
        File downloadedFile = TestAPI.getFileUploaded(project, jobUUID, SOURCECODE_ZIP);
        assertNotNull(downloadedFile);
        assertTrue(downloadedFile.exists());
        return this;
    }

    public AssertUser canUploadBinariesTarFile(TestProject project, UUID jobUUID, String pathInsideResources) {
        as(user).uploadBinaries(project, jobUUID, pathInsideResources);
        /* check if file is uploaded on server location */
        File downloadedFile = TestAPI.getFileUploaded(project, jobUUID, BINARIES_TAR);
        assertNotNull(downloadedFile);
        assertTrue(downloadedFile.exists());
        return this;
    }

    public AssertJobInformationAdministration<AssertUser> onJobAdministration() {
        return new AssertJobInformationAdministration<AssertUser>(this, user);
    }

    public AssertJobScheduler<AssertUser> onJobScheduling(TestProject project) {
        return new AssertJobScheduler<AssertUser>(this, user, project);
    }

    private void assertUserHasRole(String role, boolean shallHave) {
        TestAPI.executeRunnableAndAcceptAssertionsMaximumTimes(5, () -> {

            boolean result = as(user).getBooleanFromURL(getUrlBuilder().buildCheckRole(role));
            if (shallHave) {
                if (!result) {
                    fail("User:" + user.getUserId() + " does not have role '" + role + "' !");
                }
            } else {
                if (result) {
                    fail("User:" + user.getUserId() + " has role '" + role + "' !");
                }
            }

        }, 300);

    }

    public AssertUser hasUserRole() {
        assertUserHasRole("user", true);
        return this;
    }

    public AssertUser hasOwnerRole() {
        assertUserHasRole("owner", true);
        return this;
    }

    public AssertUser hasNotUserRole() {
        assertUserHasRole("user", false);
        return this;
    }

    public AssertUser hasNotOwnerRole() {
        assertUserHasRole("owner", false);
        return this;
    }

    public AssertUser isOwnerOf(TestProject project) {
        assertProject(project).hasOwner(user);
        assertTrue(checkIsOwnerOfProject(project, fetchUserDetails())); // test user.ownedProjects
        return this;
    }

    public AssertUser isNotOwnerOf(TestProject project) {
        assertProject(project).hasNotOwner(user); // test project.owner
        assertFalse(checkIsOwnerOfProject(project, fetchUserDetails())); // test user.ownedProjects
        return this;
    }

    public AssertUser canDownloadReportForJob(TestProject project, UUID jobUUID) {
        as(user).getStringFromURL(getUrlBuilder().buildGetJobReportUrl(project.getProjectId(), jobUUID));
        return this;
    }

    public AssertUser canNotDownloadReportForJob(TestProject project, UUID jobUUID) {
        expectHttpFailure(() -> as(user).getStringFromURL(getUrlBuilder().buildGetJobReportUrl(project.getProjectId(), jobUUID)), HttpStatus.NOT_FOUND);
        return this;
    }

    public AssertUser canGrantSuperAdminRightsTo(TestUser targetUser) {
        as(user).grantSuperAdminRightsTo(targetUser);
        return this;
    }

    public AssertUser canNotGrantSuperAdminRightsTo(TestUser targetUser, HttpStatus expected) {
        expectHttpFailure(() -> as(user).grantSuperAdminRightsTo(targetUser), expected);
        return this;
    }

    public AssertUser canRevokeSuperAdminRightsTo(TestUser targetUser) {
        as(user).revokeSuperAdminRightsFrom(targetUser);
        return this;
    }

    public AssertUser canNotRevokeSuperAdminRightsFrom(TestUser targetUser, HttpStatus expected) {
        expectHttpFailure(() -> as(user).revokeSuperAdminRightsFrom(targetUser), expected);
        return this;
    }

    public AssertUser hasReceivedEmail(String subject) {
        AssertMail.assertMailExists(user.getEmail(), subject);
        return this;
    }

    public AssertUser hasReceivedEmail(String subject, TextSearchMode subjectSearchMode) {
        AssertMail.assertMailExists(user.getEmail(), subject, subjectSearchMode);
        return this;
    }

    public AssertUser isWaitingForSignup() {
        return isWaitingForSignup(true);
    }

    private AssertUser isWaitingForSignup(boolean expectWaiting) {

        int maxTries = 20;
        boolean foundWaiting = !expectWaiting;
        int tries = 0;
        while (foundWaiting != expectWaiting) {
            tries++;
            if (tries > maxTries) {
                fail("Signup not found for user:" + user.getUserId());
                return this;
            }
            TestAPI.waitMilliSeconds(300);

            SortedMap<String, String> signups = TestAPI.listSignups();
            String email = signups.get(user.getUserId());
            foundWaiting = user.getEmail().equals(email);
        }

        return this;
    }

    public AssertUser canSetMockConfiguration(TestProject project, String json) {
        as(user).setProjectMockConfiguration(project, json);
        return this;
    }

    public void canNotSetMockConfiguration(TestProject project, String json, HttpStatus expected) {
        expectHttpFailure(() -> as(user).setProjectMockConfiguration(project, json), expected);
    }

    public AssertMapping canGetMapping(String mappingId) {
        MappingData mappingData = as(user).getMappingData(mappingId);
        return new AssertMapping(mappingData);
    }

    /**
     * Waits maximum 5 seconds for job being done {@link TestExecutionState#ENDED}.
     * A precondition is that the job must exist before calling this method.
     *
     * @param project
     * @param jobUUID
     * @return assert object
     */
    public AssertUser waitForJobDone(TestProject project, UUID jobUUID) {
        long start = System.currentTimeMillis();
        int failed = 0;
        boolean atLeastOneTimeOkay = false;
        while (!atLeastOneTimeOkay && failed < 10) {
            if (onJobScheduling(project).canFindJob(jobUUID).hasExecutionState(TestExecutionState.ENDED)) {
                atLeastOneTimeOkay = true;
            } else {
                failed++;
                waitMilliSeconds(500);
            }
        }
        if (!atLeastOneTimeOkay) {
            fail("wait for job " + jobUUID + " for project " + project + " timed out:" + (System.currentTimeMillis() - start) + " ms elapsed");
        }
        return this;
    }

}
