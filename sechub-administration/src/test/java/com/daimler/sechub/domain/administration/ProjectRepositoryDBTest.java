// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.daimler.sechub.domain.administration.project.Project;
import com.daimler.sechub.domain.administration.project.ProjectRepository;
import com.daimler.sechub.domain.administration.project.TestProjectCreationFactory;
import com.daimler.sechub.domain.administration.user.TestUserCreationFactory;
import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.domain.administration.user.UserRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ProjectRepository.class, ProjectRepositoryDBTest.SimpleTestConfiguration.class })
public class ProjectRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;

    @Before
    public void before() {
        user1 = TestUserCreationFactory.createUser("db_test_testuser1");
        user1 = entityManager.persistAndFlush(user1);
    }

    @Test
    public void project_with_owner_can_be_deleted() {
        /* prepare */
        Project project = TestProjectCreationFactory.createProject("project_repo_test1", user1);

        entityManager.persist(project);

        /* execute */
        projectRepository.deleteProjectWithAssociations(project.getId());

        /* test */
        prepareForDBTest();
        assertProjectNotFound(project);

        assertUserFound(user1);

    }

    @Test
    public void project_with_owner_and_users_can_be_deleted() {
        /* prepare */
        Project project = TestProjectCreationFactory.createProject("project_repo_test2", user1);

        project.getUsers().add(user1);
        entityManager.persist(project);

        prepareForDBTest();

        /* execute */
        projectRepository.deleteProjectWithAssociations(project.getId());

        prepareForDBTest();
        assertProjectNotFound(project);

        assertUserFound(user1);

    }

    @Test
    public void project_with_owner_whitelists_and_users_can_be_deleted__user_still_exists() throws Exception {
        /* prepare */
        Project project = TestProjectCreationFactory.createProject("project_repo_test2", user1);

        project.getUsers().add(user1);
        project.getWhiteList().add(new URI("http://www.example.org"));
        entityManager.persist(project);
        entityManager.persist(user1);

        prepareForDBTest();

        /* execute */
        projectRepository.deleteProjectWithAssociations(project.getId());

        prepareForDBTest();
        assertProjectNotFound(project);

        assertUserFound(user1);

    }

    /**
     * This will flush data to DB and also clear entity manager, so cached objects
     * will be removed and we got fresh data from DB
     */
    private void prepareForDBTest() {
        entityManager.flush();
        entityManager.clear();
    }

    private void assertUserFound(User user) {
        Optional<User> userFound = userRepository.findById(user.getName());
        assertTrue(userFound.isPresent());
    }

    private void assertProjectNotFound(Project project) {
        Optional<Project> found = projectRepository.findById(project.getId());
        assertFalse(found.isPresent());
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
