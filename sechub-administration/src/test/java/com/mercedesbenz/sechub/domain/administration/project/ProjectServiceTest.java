// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class ProjectServiceTest {

    private static ProjectService serviceToTest;
    private static UserRepository userRepository;

    @BeforeAll
    static void beforeAll() {
        userRepository = mock();
        UserInputAssertion userInputAssertion = mock();

        serviceToTest = new ProjectService(userRepository, userInputAssertion);
    }

    @ParameterizedTest
    @ValueSource(strings = { "user1", "user3" })
    void get_projects_user_is_super_admin_and_see_all_projects_with_all_information(String userId) {
        /* prepare */
        setUpTestCase(userId, true);

        /* execute */
        List<ProjectData> projects = serviceToTest.getProjectDataList(userId);

        /* test */
        assertNotNull(projects);
        assertEquals(1, projects.size());

        // the order is not guaranteed, so we can not check for other properties,
        // but we can check if the assigned users are present for all projects
        assertNotNull(projects.get(0).getAssignedUsers());
    }

    @Test
    void get_projects_user1_and_see_all_owned_projects_with_all_information() {
        /* prepare */
        String userId = "user1";

        setUpTestCase(userId, false);

        /* execute */
        List<ProjectData> projects = serviceToTest.getProjectDataList(userId);

        /* test */
        assertNotNull(projects);
        assertEquals(1, projects.size());

        assertEquals("project1", projects.get(0).getProjectId());
        assertEquals("user1@mail", projects.get(0).getOwner());
        assertTrue(projects.get(0).isOwned());
        assertEquals(0, projects.get(0).getAssignedUsers().length);
    }

    @Test
    void get_projects_user2_and_see_all_owned_projects_with_all_information() {
        /* prepare */
        String userId = "user2";

        setUpTestCase(userId, false);

        /* execute */
        List<ProjectData> projects = serviceToTest.getProjectDataList(userId);

        /* test */
        assertNotNull(projects);
        assertEquals(2, projects.size());

        assertEquals("user2@mail", projects.get(0).getOwner());
        assertTrue(projects.get(0).isOwned());
        assertEquals(0, projects.get(0).getAssignedUsers().length);

        assertEquals("user2@mail", projects.get(1).getOwner());
        assertTrue(projects.get(1).isOwned());
        assertTrue(projects.get(1).getAssignedUsers().length > 0);
    }

    @Test
    void get_projects_user3_and_see_assigned_project_with_owner_but_no_users() {
        /* prepare */
        String userId = "user3";

        setUpTestCase(userId, false);

        /* execute */
        List<ProjectData> projects = serviceToTest.getProjectDataList(userId);

        /* test */
        assertNotNull(projects);
        assertEquals(1, projects.size());

        assertEquals("project2", projects.get(0).getProjectId());
        assertEquals("user2@mail", projects.get(0).getOwner());
        assertFalse(projects.get(0).isOwned());
        assertNull(projects.get(0).getAssignedUsers());
    }

    private void setUpTestCase(String userId, boolean isAdmin) {
        // test case:
        // user1 is owner of project1
        // user2 is owner of project2 and project3
        // user3 is assigned to project2

        User user1 = mock(User.class);
        User user2 = mock(User.class);
        User user3 = mock(User.class);

        Project project1 = mock(Project.class);
        Project project2 = mock(Project.class);
        Project project3 = mock(Project.class);

        when(user1.getName()).thenReturn("user1");
        when(user1.getEmailAddress()).thenReturn("user1@mail");
        when(user2.getName()).thenReturn("user2");
        when(user2.getEmailAddress()).thenReturn("user2@mail");
        when(user3.getName()).thenReturn("user3");
        when(user3.getEmailAddress()).thenReturn("user3@mail");

        when(user1.getProjects()).thenReturn(Set.of(project1));
        when(user1.getOwnedProjects()).thenReturn(Set.of(project1));
        when(user2.getProjects()).thenReturn(Set.of(project2, project3));
        when(user2.getOwnedProjects()).thenReturn(Set.of(project2, project3));
        when(user3.getProjects()).thenReturn(Set.of(project2));
        when(user3.getOwnedProjects()).thenReturn(Set.of());

        when(project1.getOwner()).thenReturn(user1);
        when(project2.getOwner()).thenReturn(user2);
        when(project3.getOwner()).thenReturn(user2);

        when(project1.getId()).thenReturn("project1");
        when(project2.getId()).thenReturn("project2");
        when(project3.getId()).thenReturn("project3");

        Set<User> users = Set.of(user2, user3);
        when(project2.getUsers()).thenReturn(users);

        switch (userId) {
        case "user1" -> {
            when(user1.isSuperAdmin()).thenReturn(isAdmin);
            when(userRepository.findOrFailUser(userId)).thenReturn(user1);
        }
        case "user2" -> {
            when(user2.isSuperAdmin()).thenReturn(isAdmin);
            when(userRepository.findOrFailUser("user2")).thenReturn(user2);
        }
        case "user3" -> {
            when(user3.isSuperAdmin()).thenReturn(isAdmin);
            when(userRepository.findOrFailUser("user3")).thenReturn(user3);
        }
        }
    }
}