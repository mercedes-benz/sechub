// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static com.mercedesbenz.sechub.domain.administration.user.TestUserCreationFactory.createProjectUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
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
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class ProjectServiceTest {

    private static final UserRepository userRepository = mock();
    private static final UserInputAssertion userInputAssertion = mock();
    private static final ProjectService serviceToTest = new ProjectService(userRepository, userInputAssertion);

    @BeforeEach
    void beforeEach() {
        Mockito.reset(userRepository);
        setUpTestCase();
    }

    @ParameterizedTest
    @ArgumentsSource(ProjectServiceArgumentsProvider.class)
    void users_receive_expected_number_of_projects(String userId, int expectedProjects, boolean isOwner, String expectedOwner) {
        /* execute */
        List<ProjectData> projects = serviceToTest.getAssignedProjectDataList(userId);

        /* test */
        assertThat(projects).isNotNull();
        assertThat(projects.size()).isEqualTo(expectedProjects);

        for (ProjectData project : projects) {
            assertThat(project.isOwned()).isEqualTo(isOwner);
            assertThat(project.getOwner()).isEqualTo(expectedOwner);
        }
    }

    @Test
    void user2_sees_assigned_and_owned_projects_with_users() {
        /* prepare */
        String userId = "user2";

        /* execute */
        List<ProjectData> projects = serviceToTest.getAssignedProjectDataList(userId);

        /* test */
        assertThat(projects.get(0).getAssignedUsers()).isNotNull();
        assertThat(projects.get(1).getAssignedUsers()).isNotNull();

        // List of projects is unsorted
        assertThat(projects.get(0).getProjectId()).isIn("project2", "project3");
        assertThat(projects.get(1).getProjectId()).isIn("project2", "project3");

        assertThat(projects.get(0).getAssignedUsers().length).isGreaterThan(0);
        assertThat(projects.get(1).getAssignedUsers().length).isGreaterThan(0);
    }

    @Test
    void user3_sees_assigned_project_without_users() {
        /* prepare */
        String userId = "user3";

        /* execute */
        List<ProjectData> projects = serviceToTest.getAssignedProjectDataList(userId);

        /* test */
        assertThat(projects.get(0).getProjectId()).isEqualTo("project2");
        assertThat(projects.get(0).getAssignedUsers()).isNull();
    }

    @Test
    void user4_is_admin_and_sees_assigned_project_with_users() {
        /* prepare */
        String userId = "user4";

        /* execute */
        List<ProjectData> projects = serviceToTest.getAssignedProjectDataList(userId);

        /* test */
        assertThat(projects.get(0).getProjectId()).isEqualTo("project2");
        assertThat(projects.get(0).getAssignedUsers().length).isEqualTo(3);
        assertThat(projects.get(0).getAssignedUsers()).contains("user2@example.org", "user3@example.org", "user4@example.org");
    }

    private void setUpTestCase() {
        // test case:
        // user1 is owner of project1, project1 has no users, user1 is super admin
        // user2 is owner of project2 and project3 and assigned to project2 and project3
        // user3 is assigned to project2
        // user4 is assigned to project2 and is super admin

        Project project1 = createProject("project1");
        Project project2 = createProject("project2");
        Project project3 = createProject("project3");

        User user1 = createProjectUser("user1", Set.of(), false);
        User user2 = createProjectUser("user2", Set.of(project2, project3), false);
        User user3 = createProjectUser("user3", Set.of(project2), false);
        User user4 = createProjectUser("user4", Set.of(project2), true);

        project1.owner = user1;

        project2.owner = user2;
        project2.users = Set.of(user2, user3, user4);

        project3.owner = user2;
        project3.users = Set.of(user2);

        when(userRepository.findOrFailUser("user1")).thenReturn(user1);
        when(userRepository.findOrFailUser("user2")).thenReturn(user2);
        when(userRepository.findOrFailUser("user3")).thenReturn(user3);
        when(userRepository.findOrFailUser("user4")).thenReturn(user4);
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
                    Arguments.of("user1", 0, true, "user1@example.org"),
                    Arguments.of("user2", 2, true, "user2@example.org"),
                    Arguments.of("user3", 1, false, "user2@example.org"),
                    Arguments.of("user4", 1, false, "user2@example.org"));
        }
        /* @formatter:on */
    }

}