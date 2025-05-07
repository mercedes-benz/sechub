// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static com.mercedesbenz.sechub.domain.administration.user.TestUserCreationFactory.*;
import static com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class ProjectServiceTest {

    /**
     * Owner: {@link #USER_ID_1} Users: {@link #USER_ID_1}
     */
    private static final String PROJECT_ID_1 = "project1";

    /**
     * Owner: {@link #USER_ID_2} Users: {@value #USER_ID_2, #USER_ID_3,
     * #USER_ID_4_ADMIN}
     */
    private static final String PROJECT_ID_2 = "project2";

    /**
     * Owner: {@link #USER_ID_2} Users: {@value #USER_ID_2
     */
    private static final String PROJECT_ID_3 = "project3";

    /**
     * Owner: {@link #USER_ID_5_ONLY_OWNER} Users: (none)
     */
    private static final String PROJECT_ID_4 = "project4";

    private static final String USER_ID_0_NOT_ASSIGNED_OR_OWNER = "user0";

    /**
     * Assigned to {@link #PROJECT_ID_2}, {@link #PROJECT_ID_3} <br>
     * Owner of {@link #PROJECT_ID_2}, {@link #PROJECT_ID_3}
     */
    private static final String USER_ID_1 = "user1";

    /**
     * Assigned to {@link #PROJECT_ID_1 <br>
     * Owner of {@link #PROJECT_ID_1}
     */
    private static final String USER_ID_2 = "user2";

    /**
     * Assigned to {@link #PROJECT_ID_2 <br>
     * Owner of (none)
     */
    private static final String USER_ID_3 = "user3";

    /**
     * An administrator can be assigned as a user to a project as well. Assigned to
     * {@link #PROJECT_ID_2 <br>
     * Owner of (none)
     */
    private static final String USER_ID_4_ADMIN = "user4";

    /* This user is owner of project4 - but not assigned */
    private static final String USER_ID_5_ONLY_OWNER = "user5";

    private static final UserRepository userRepository = mock();
    private static final ProjectRepository projectRepository = mock();
    private static final UserInputAssertion userInputAssertion = mock();
    private static final DomainMessageService eventBus = mock();
    private static final ProjectService serviceToTest = new ProjectService(userRepository, projectRepository, userInputAssertion, eventBus);

    @BeforeEach
    void beforeEach() {
        Mockito.reset(userRepository);
        Mockito.reset(projectRepository);
        Mockito.reset(eventBus);

        setUpTestCase();
    }

    @ParameterizedTest
    @ArgumentsSource(ProjectServiceArgumentsProvider.class)
    void getProjectDataList_users_receive_expected_project_data(String variant, String userId, Set<String> projectIds, boolean isOwner,
            String expectedOwnerUserId, String expectedOwnerEmailAddress) {
        /* prepare */
        DomainMessageSynchronousResult mockedResponse = mock();
        when(mockedResponse.get(PROJECT_ASSIGNED_AND_ENABLED_PROFILE_IDS)).thenReturn(Collections.emptyMap());
        when(eventBus.sendSynchron(any())).thenReturn(mockedResponse);

        /* execute */
        List<ProjectData> projects = serviceToTest.getProjectDataList(userId);

        /* test */
        assertThat(projects).isNotNull();
        assertThat(projects.size()).isEqualTo(projectIds.size());

        for (ProjectData project : projects) {
            ProjectUserData expectedOwnerData = new ProjectUserData();
            expectedOwnerData.setUserId(expectedOwnerUserId);
            expectedOwnerData.setEmailAddress(expectedOwnerEmailAddress);

            assertThat(project.isOwned()).isEqualTo(isOwner);
            assertThat(project.getOwner()).isEqualTo(expectedOwnerData);

            assertThat(project).matches(p -> projectIds.contains(p.getProjectId()));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ProjectServiceArgumentsProvider.class)
    void getProjectDataList_expected_events_are_sent(String variant, String userId, Set<String> projectIds, boolean isOwner, String expectedOwnerUserId,
            String expectedOwnerEmailAddress) {
        /* prepare */
        DomainMessageSynchronousResult mockedResponse = mock();
        when(mockedResponse.get(PROJECT_ASSIGNED_AND_ENABLED_PROFILE_IDS)).thenReturn(Collections.emptyMap());
        when(eventBus.sendSynchron(any())).thenReturn(mockedResponse);

        /* execute */
        serviceToTest.getProjectDataList(userId);

        /* test */
        ArgumentCaptor<DomainMessage> captor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(eventBus).sendSynchron(captor.capture());

        DomainMessage sentMessage = captor.getValue();
        assertThat(sentMessage).isNotNull();
        assertThat(sentMessage.getMessageId()).isEqualTo(MessageID.REQUEST_ENABLED_PROFILE_IDS_FOR_PROJECTS);
        assertThat(sentMessage.get(MessageDataKeys.PROJECT_IDS)).containsAll(projectIds);
    }

    @Test
    void getProjectDataList_user2_sees_assigned_and_owned_projects_with_users_and_with_assigned_profile_ids() {
        /* prepare */
        String userId = USER_ID_2;

        List<String> profileIdsProject2 = List.of("profile1", "profile5", "profile12");
        DomainMessageSynchronousResult responseProject2 = prepareValidSyncResult(PROJECT_ID_2, profileIdsProject2);
        when(eventBus.sendSynchron(any())).thenReturn(responseProject2);

        /* execute */
        List<ProjectData> projects = serviceToTest.getProjectDataList(userId);

        /* test */
        assertThat(projects).hasSize(2);
        assertThat(projects.get(0).getAssignedUsers()).isNotNull();
        assertThat(projects.get(1).getAssignedUsers()).isNotNull();

        // List of projects is unsorted
        assertThat(projects.get(0).getProjectId()).isIn(PROJECT_ID_2, PROJECT_ID_3);
        assertThat(projects.get(1).getProjectId()).isIn(PROJECT_ID_2, PROJECT_ID_3);

        assertThat(projects.get(0).getAssignedUsers().size()).isGreaterThan(0);
        assertThat(projects.get(1).getAssignedUsers().size()).isGreaterThan(0);

        // verify profile IDs are included correctly
        boolean assertionDone = false;
        for (ProjectData projectData : projects) {
            if (PROJECT_ID_2.equals(projectData.getProjectId())) {
                assertThat(projectData.getEnabledProfileIds()).containsAll(profileIdsProject2);
                assertionDone = true;
            }
        }
        assertThat(assertionDone).isTrue();
    }

    @Test
    void getProjectDataList_user3_sees_assigned_project_without_users() {
        /* prepare */
        String userId = USER_ID_3;
        DomainMessageSynchronousResult mockedResponse = mock();
        when(mockedResponse.get(PROJECT_ASSIGNED_AND_ENABLED_PROFILE_IDS)).thenReturn(Collections.emptyMap());
        when(eventBus.sendSynchron(any())).thenReturn(mockedResponse);

        /* execute */
        List<ProjectData> projects = serviceToTest.getProjectDataList(userId);

        /* test */
        assertThat(projects.get(0).getProjectId()).isEqualTo(PROJECT_ID_2);
        assertThat(projects.get(0).getAssignedUsers()).isNull();
    }

    @Test
    void getProjectDataList_user4_is_admin_and_sees_assigned_project_with_users() {
        /* prepare */
        String userId = USER_ID_4_ADMIN;
        DomainMessageSynchronousResult mockedResponse = mock();
        when(mockedResponse.get(PROJECT_ASSIGNED_AND_ENABLED_PROFILE_IDS)).thenReturn(Collections.emptyMap());
        when(eventBus.sendSynchron(any())).thenReturn(mockedResponse);

        /* execute */
        List<ProjectData> projects = serviceToTest.getProjectDataList(userId);

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
        user4.setUserId(USER_ID_4_ADMIN);

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
        Project project4 = createProject(PROJECT_ID_4);

        User user0 = createProjectUser(USER_ID_0_NOT_ASSIGNED_OR_OWNER, false);
        User user1 = createProjectUser(USER_ID_1, false);
        User user2 = createProjectUser(USER_ID_2, false);
        User user3 = createProjectUser(USER_ID_3, false);
        User user4_admin = createProjectUser(USER_ID_4_ADMIN, true);
        User user5 = createProjectUser(USER_ID_5_ONLY_OWNER, false);

        when(userRepository.findOrFailUser(USER_ID_0_NOT_ASSIGNED_OR_OWNER)).thenReturn(user0);
        when(userRepository.findOrFailUser(USER_ID_1)).thenReturn(user1);
        when(userRepository.findOrFailUser(USER_ID_2)).thenReturn(user2);
        when(userRepository.findOrFailUser(USER_ID_3)).thenReturn(user3);
        when(userRepository.findOrFailUser(USER_ID_4_ADMIN)).thenReturn(user4_admin);
        when(userRepository.findOrFailUser(USER_ID_5_ONLY_OWNER)).thenReturn(user5);

        when(projectRepository.findAllProjectIdsWhereUserIsAssigned(USER_ID_0_NOT_ASSIGNED_OR_OWNER)).thenReturn(Set.of());
        when(projectRepository.findAllProjectIdsWhereUserIsAssigned(USER_ID_1)).thenReturn(Set.of(project1.getId()));
        when(projectRepository.findAllProjectIdsWhereUserIsAssigned(USER_ID_2)).thenReturn(Set.of(project2.getId(), project3.getId()));
        when(projectRepository.findAllProjectIdsWhereUserIsAssigned(USER_ID_3)).thenReturn(Set.of(project2.getId()));
        when(projectRepository.findAllProjectIdsWhereUserIsAssigned(USER_ID_4_ADMIN)).thenReturn(Set.of(project2.getId()));
        when(projectRepository.findAllProjectIdsWhereUserIsAssigned(USER_ID_5_ONLY_OWNER)).thenReturn(Set.of());

        when(projectRepository.findAllProjectIdsWhereUserIsOwner(USER_ID_0_NOT_ASSIGNED_OR_OWNER)).thenReturn(Set.of());
        when(projectRepository.findAllProjectIdsWhereUserIsOwner(USER_ID_1)).thenReturn(Set.of(project1.getId()));
        when(projectRepository.findAllProjectIdsWhereUserIsOwner(USER_ID_2)).thenReturn(Set.of(project2.getId(), project3.getId()));
        when(projectRepository.findAllProjectIdsWhereUserIsOwner(USER_ID_3)).thenReturn(Set.of());
        when(projectRepository.findAllProjectIdsWhereUserIsOwner(USER_ID_4_ADMIN)).thenReturn(Set.of());
        when(projectRepository.findAllProjectIdsWhereUserIsOwner(USER_ID_5_ONLY_OWNER)).thenReturn(Set.of(project4.getId()));

        when(projectRepository.findOrFailProject(PROJECT_ID_1)).thenReturn(project1);
        when(projectRepository.findOrFailProject(PROJECT_ID_2)).thenReturn(project2);
        when(projectRepository.findOrFailProject(PROJECT_ID_3)).thenReturn(project3);
        when(projectRepository.findOrFailProject(PROJECT_ID_4)).thenReturn(project4);

        when(projectRepository.fetchProjectUserDataForOwner(PROJECT_ID_1)).thenReturn(new ProjectUserData(user1.getName(), user1.getEmailAddress()));
        when(projectRepository.fetchProjectUserDataForOwner(PROJECT_ID_2)).thenReturn(new ProjectUserData(user2.getName(), user2.getEmailAddress()));
        when(projectRepository.fetchProjectUserDataForOwner(PROJECT_ID_3)).thenReturn(new ProjectUserData(user2.getName(), user2.getEmailAddress()));
        when(projectRepository.fetchProjectUserDataForOwner(PROJECT_ID_4)).thenReturn(new ProjectUserData(user5.getName(), user5.getEmailAddress()));

        when(projectRepository.fetchOrderedProjectUserDataForAssignedUsers(PROJECT_ID_1))
                .thenReturn(List.of(new ProjectUserData(user2.getName(), user2.getEmailAddress())));
        when(projectRepository.fetchOrderedProjectUserDataForAssignedUsers(PROJECT_ID_2)).thenReturn(
                List.of(new ProjectUserData(user2.getName(), user2.getEmailAddress()), new ProjectUserData(user3.getName(), user3.getEmailAddress()),
                        new ProjectUserData(user4_admin.getName(), user4_admin.getEmailAddress())));
        when(projectRepository.fetchOrderedProjectUserDataForAssignedUsers(PROJECT_ID_3))
                .thenReturn(List.of(new ProjectUserData(user2.getName(), user2.getEmailAddress())));
        when(projectRepository.fetchOrderedProjectUserDataForAssignedUsers(PROJECT_ID_4)).thenReturn(List.of());

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
                    /* variant, user id, projectsAmount, isOwner, expectedOwnerId, expectedOwnerMailAddress*/
                    Arguments.of("a0", USER_ID_0_NOT_ASSIGNED_OR_OWNER, Set.of(), false,null, null),
                    Arguments.of("a1", USER_ID_1, Set.of(PROJECT_ID_1), true, USER_ID_1, "user1@example.org"),
                    Arguments.of("a2", USER_ID_2, Set.of(PROJECT_ID_3, PROJECT_ID_2), true, USER_ID_2, "user2@example.org"),
                    Arguments.of("a3", USER_ID_3, Set.of(PROJECT_ID_2), false, USER_ID_2, "user2@example.org"),
                    Arguments.of("a4", USER_ID_4_ADMIN, Set.of(PROJECT_ID_2), false, USER_ID_2, "user2@example.org"),
                    Arguments.of("a5", USER_ID_5_ONLY_OWNER,Set.of(PROJECT_ID_4), true, USER_ID_5_ONLY_OWNER, "user5@example.org"));
        }
        /* @formatter:on */
    }

    private DomainMessageSynchronousResult prepareValidSyncResult(String projectId, List<String> profileIds) {
        DomainMessageSynchronousResult response = new DomainMessageSynchronousResult(MessageID.REQUEST_ENABLED_PROFILE_IDS_FOR_PROJECTS);
        response.set(PROJECT_ASSIGNED_AND_ENABLED_PROFILE_IDS, Map.of(projectId, profileIds));
        return response;
    }
}