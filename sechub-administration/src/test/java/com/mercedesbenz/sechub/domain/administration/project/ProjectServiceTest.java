// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static com.mercedesbenz.sechub.domain.administration.user.TestUserCreationFactory.createProjectUser;
import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.PROJECT_ASSIGNED_PROFILE_IDS;
import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.PROJECT_IDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class ProjectServiceTest {

    private static final String PROJECT_ID_1 = "project1";
    private static final String PROJECT_ID_2 = "project2";
    private static final String PROJECT_ID_3 = "project3";

    private static final String USER_ID_1 = "user1";
    private static final String USER_ID_2 = "user2";
    private static final String USER_ID_3 = "user3";
    private static final String USER_ID_4 = "user4";

    private static final UserRepository userRepository = mock();
    private static final UserInputAssertion userInputAssertion = mock();
    private static final DomainMessageService eventBus = mock();
    private static final ProjectService serviceToTest = new ProjectService(userRepository, userInputAssertion, eventBus);

    @BeforeEach
    void beforeEach() {
        Mockito.reset(userRepository);
        setUpTestCase();
    }

    @ParameterizedTest
    @ArgumentsSource(ProjectServiceArgumentsProvider.class)
    void users_receive_expected_number_of_projects(String userId, int expectedProjects, boolean isOwner, String expectedOwnerUserId,
            String expectedOwnerEmailAddress) {
        /* prepare */
        DomainMessageSynchronousResult mockedResponse = mock();
        when(mockedResponse.get(PROJECT_IDS)).thenReturn(List.of("mocked-project"));
        when(mockedResponse.get(PROJECT_ASSIGNED_PROFILE_IDS)).thenReturn(Collections.emptyMap());
        when(eventBus.sendSynchron(any())).thenReturn(mockedResponse);

        /* execute */
        List<ProjectData> projects = serviceToTest.getAssignedProjectDataList(userId);

        /* test */
        assertThat(projects).isNotNull();
        assertThat(projects.size()).isEqualTo(expectedProjects);

        for (ProjectData project : projects) {
            ProjectUserData expectedOwnerData = new ProjectUserData();
            expectedOwnerData.setUserId(expectedOwnerUserId);
            expectedOwnerData.setEmailAddress(expectedOwnerEmailAddress);

            assertThat(project.isOwned()).isEqualTo(isOwner);
            assertThat(project.getOwner()).isEqualTo(expectedOwnerData);
        }
    }

    @Test
    void user2_sees_assigned_and_owned_projects_with_users_and_with_assigned_profile_ids() {
        /* prepare */
        String userId = USER_ID_2;

        List<String> profileIdsProject2 = List.of("profile1", "profile5", "profile12");
        DomainMessageSynchronousResult responseProject2 = prepareValidSyncResult(PROJECT_ID_2, profileIdsProject2);
        when(eventBus.sendSynchron(any())).thenReturn(responseProject2);

        /* execute */
        List<ProjectData> projects = serviceToTest.getAssignedProjectDataList(userId);

        /* test */
        assertThat(projects.get(0).getAssignedUsers()).isNotNull();
        assertThat(projects.get(1).getAssignedUsers()).isNotNull();

        // List of projects is unsorted
        assertThat(projects.get(0).getProjectId()).isIn(PROJECT_ID_2, PROJECT_ID_3);
        assertThat(projects.get(1).getProjectId()).isIn(PROJECT_ID_2, PROJECT_ID_3);

        assertThat(projects.get(0).getAssignedUsers().size()).isGreaterThan(0);
        assertThat(projects.get(1).getAssignedUsers().size()).isGreaterThan(0);

        // verify profile IDs are included correctly
        for (ProjectData projectData : projects) {
            if (PROJECT_ID_2.equals(projectData.getProjectId())) {
                assertThat(projectData.getAssignedProfileIds()).containsAll(profileIdsProject2);
            }
        }
    }

    @Test
    void user3_sees_assigned_project_without_users() {
        /* prepare */
        String userId = USER_ID_3;
        DomainMessageSynchronousResult mockedResponse = mock();
        when(mockedResponse.get(PROJECT_IDS)).thenReturn(List.of("mocked-project"));
        when(mockedResponse.get(PROJECT_ASSIGNED_PROFILE_IDS)).thenReturn(Collections.emptyMap());
        when(eventBus.sendSynchron(any())).thenReturn(mockedResponse);

        /* execute */
        List<ProjectData> projects = serviceToTest.getAssignedProjectDataList(userId);

        /* test */
        assertThat(projects.get(0).getProjectId()).isEqualTo(PROJECT_ID_2);
        assertThat(projects.get(0).getAssignedUsers()).isNull();
    }

    @Test
    void user4_is_admin_and_sees_assigned_project_with_users() {
        /* prepare */
        String userId = USER_ID_4;
        DomainMessageSynchronousResult mockedResponse = mock();
        when(mockedResponse.get(PROJECT_IDS)).thenReturn(List.of("mocked-project"));
        when(mockedResponse.get(PROJECT_ASSIGNED_PROFILE_IDS)).thenReturn(Collections.emptyMap());
        when(eventBus.sendSynchron(any())).thenReturn(mockedResponse);

        /* execute */
        List<ProjectData> projects = serviceToTest.getAssignedProjectDataList(userId);

        /* test */
        ProjectData project2Data = projects.get(0);
        assertThat(project2Data.getProjectId()).isEqualTo(PROJECT_ID_2);
        assertThat(project2Data.getAssignedUsers().size()).isEqualTo(3);

        ProjectUserData user2 = new ProjectUserData();
        user2.setEmailAddress("user2@example.org");
        user2.setUserId(USER_ID_2);

        ProjectUserData user3 = new ProjectUserData();
        user3.setEmailAddress("user3@example.org");
        user3.setUserId(USER_ID_3);

        ProjectUserData user4 = new ProjectUserData();
        user4.setEmailAddress("user4@example.org");
        user4.setUserId(USER_ID_4);

        assertThat(project2Data.getAssignedUsers()).contains(user2, user3, user4);
    }

    private void setUpTestCase() {
        // test case:
        // user1 is owner of project1, project1 has no users, user1 is super admin
        // user2 is owner of project2 and project3 and assigned to project2 and project3
        // user3 is assigned to project2
        // user4 is assigned to project2 and is super admin

        Project project1 = createProject(PROJECT_ID_1);
        Project project2 = createProject(PROJECT_ID_2);
        Project project3 = createProject(PROJECT_ID_3);

        User user1 = createProjectUser(USER_ID_1, Set.of(), false);
        User user2 = createProjectUser(USER_ID_2, Set.of(project2, project3), false);
        User user3 = createProjectUser(USER_ID_3, Set.of(project2), false);
        User user4 = createProjectUser(USER_ID_4, Set.of(project2), true);

        project1.owner = user1;

        project2.owner = user2;
        project2.users = Set.of(user2, user3, user4);

        project3.owner = user2;
        project3.users = Set.of(user2);

        when(userRepository.findOrFailUser(USER_ID_1)).thenReturn(user1);
        when(userRepository.findOrFailUser(USER_ID_2)).thenReturn(user2);
        when(userRepository.findOrFailUser(USER_ID_3)).thenReturn(user3);
        when(userRepository.findOrFailUser(USER_ID_4)).thenReturn(user4);
    }

    private Project createProject(String projectId) {
        Project project = new Project();
        project.id = projectId;
        return project;
    }

    private static class ProjectServiceArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            /* @formatter:off */
            return Stream.of(
                    /* user id, expectedProjects, isOwner, expectedOwnerId, expectedOwnerMailAddress*/
                    Arguments.of(USER_ID_1, 0, true, USER_ID_1, "user1@example.org"),
                    Arguments.of(USER_ID_2, 2, true, USER_ID_2, "user2@example.org"),
                    Arguments.of(USER_ID_3, 1, false, USER_ID_2, "user2@example.org"),
                    Arguments.of(USER_ID_4, 1, false, USER_ID_2, "user2@example.org"));
        }
        /* @formatter:on */
    }

    private DomainMessageSynchronousResult prepareValidSyncResult(String projectId, List<String> profileIds) {
        DomainMessageSynchronousResult response = new DomainMessageSynchronousResult(MessageID.REQUEST_PROFILE_IDS_FOR_PROJECT);
        response.set(PROJECT_ASSIGNED_PROFILE_IDS, Map.of(projectId, profileIds));
        response.set(PROJECT_IDS, null);
        return response;
    }
}