package com.mercedesbenz.sechub.domain.administration.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class GetProjectsServiceTest {

    private GetProjectsService serviceToTest;
    private UserRepository userRepository;
    private ProjectRepository projectRepository;

    @BeforeEach
    public void before() {
        projectRepository = mock(ProjectRepository.class);
        userRepository = mock(UserRepository.class);
        UserInputAssertion userInputAssertion = mock(UserInputAssertion.class);

        serviceToTest = new GetProjectsService(userRepository, userInputAssertion, projectRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = { "user1", "user2", "user3" })
    void get_projects_user_is_super_admin_and_see_all_projects_with_all_information(String userId) {
        /* prepare */
        setUpTestCase(userId, true);

        /* execute */
        GetProjectsDTO[] projects = serviceToTest.getProjects(userId);

        /* test */
        assertNotNull(projects);
        assertEquals(3, projects.length);

        // the order is not guaranteed, so we can not check for other properties,
        // but we can check if the assigned users are present for all projects
        assertTrue(projects[0].getAssignedUsers().isPresent());
        assertTrue(projects[1].getAssignedUsers().isPresent());
        assertTrue(projects[2].getAssignedUsers().isPresent());
    }

    @Test
    void get_projects_user1_and_see_all_owned_projects_with_all_information() {
        /* prepare */
        String userId = "user1";

        setUpTestCase(userId, false);

        /* execute */
        GetProjectsDTO[] projects = serviceToTest.getProjects(userId);

        /* test */
        assertNotNull(projects);
        assertEquals(1, projects.length);

        assertEquals("project1", projects[0].getProjectId());
        assertEquals("user1", projects[0].getOwner());
        assertTrue(projects[0].isOwned());
        assertTrue(projects[0].getAssignedUsers().isPresent());
        assertEquals(0, projects[0].getAssignedUsers().get().length);
    }

    @Test
    void get_projects_user2_and_see_all_owned_projects_with_all_information() {
        /* prepare */
        String userId = "user2";

        setUpTestCase(userId, false);

        /* execute */
        GetProjectsDTO[] projects = serviceToTest.getProjects(userId);

        /* test */
        assertNotNull(projects);
        assertEquals(2, projects.length);

        assertEquals("user2", projects[0].getOwner());
        assertTrue(projects[0].isOwned());
        assertTrue(projects[0].getAssignedUsers().isPresent());

        assertEquals("user2", projects[1].getOwner());
        assertTrue(projects[1].isOwned());
        assertTrue(projects[1].getAssignedUsers().isPresent());
    }

    @Test
    void get_projects_user3_and_see_assigned_project_with_owner_but_no_users() {
        /* prepare */
        String userId = "user3";

        setUpTestCase(userId, false);

        /* execute */
        GetProjectsDTO[] projects = serviceToTest.getProjects(userId);

        /* test */
        assertNotNull(projects);
        assertEquals(1, projects.length);

        assertEquals("project2", projects[0].getProjectId());
        assertEquals("user2", projects[0].getOwner());
        assertFalse(projects[0].isOwned());
        assertFalse(projects[0].getAssignedUsers().isPresent());
    }

    private void setUpTestCase(String userId, boolean isAdmin) {
        // test case:
        // user1 is owner of project1
        // user2 is owner of project2 and project3
        // user2 has user3 assigned to project2

        User user1 = mock(User.class);
        User user2 = mock(User.class);
        User user3 = mock(User.class);

        Project project1 = mock(Project.class);
        Project project2 = mock(Project.class);
        Project project3 = mock(Project.class);

        when(user1.getName()).thenReturn("user1");
        when(user2.getName()).thenReturn("user2");
        when(user3.getName()).thenReturn("user3");

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

        when(projectRepository.findAll()).thenReturn(List.of(new Project[] { project1, project2, project3 }));
        when(projectRepository.findOrFailProject("project1")).thenReturn(project1);
        when(projectRepository.findOrFailProject("project2")).thenReturn(project2);
        when(projectRepository.findOrFailProject("project3")).thenReturn(project3);

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