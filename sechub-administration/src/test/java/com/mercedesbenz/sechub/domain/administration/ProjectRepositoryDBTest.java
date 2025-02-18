// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;

import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.domain.administration.project.ProjectRepository;
import com.mercedesbenz.sechub.domain.administration.project.TestProjectCreationFactory;
import com.mercedesbenz.sechub.domain.administration.user.TestUserCreationFactory;
import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;

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

    @BeforeEach
    void beforeEach() {
        user1 = TestUserCreationFactory.createUser("db_test_testuser1");
        user1 = entityManager.persistAndFlush(user1);
    }

    @Test
    void project_with_owner_can_be_deleted() {
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
    void project_with_owner_and_users_can_be_deleted() {
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
    void project_with_owner_whitelists_and_users_can_be_deleted__user_still_exists() throws Exception {
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

    @Test
    void two_project_with_diferent_templates_defined_delete_template1_works() throws Exception {
        /* prepare */
        Project project1 = TestProjectCreationFactory.createProject("project_repo_test3", user1);
        Project project2 = TestProjectCreationFactory.createProject("project_repo_test4", user1);

        project1.getTemplateIds().add("template1");
        project2.getTemplateIds().add("template1");
        project2.getTemplateIds().add("template2");
        entityManager.persist(project1);
        entityManager.persist(project2);

        prepareForDBTest();

        /* execute */
        projectRepository.deleteTemplateAssignmentFromAnyProject("template1");

        prepareForDBTest();

        Optional<Project> nProject1 = projectRepository.findById(project1.getId());
        assertThat(nProject1).isNotEmpty();

        Optional<Project> nProject2 = projectRepository.findById(project2.getId());
        assertThat(nProject2).isNotEmpty();

        Project p1 = nProject1.get();
        assertThat(p1.getTemplateIds()).isEmpty();

        Project p2 = nProject2.get();
        assertThat(p2.getTemplateIds()).contains("template2").hasSize(1); // template 2 assignment is not removed

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
        assertThat(userFound.isPresent()).isTrue();
    }

    private void assertProjectNotFound(Project project) {
        Optional<Project> found = projectRepository.findById(project.getId());
        assertThat(found.isPresent()).isFalse();
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
