// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.domain.administration.project.ProjectRepository;
import com.mercedesbenz.sechub.domain.administration.project.TestProjectCreationFactory;
import com.mercedesbenz.sechub.domain.administration.user.TestUserCreationFactory;
import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ProjectRepository.class, UserRepositoryDBTest.SimpleTestConfiguration.class })
public class UserRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private User user1;

    private User user2;

    @Before
    public void before() {
        user1 = TestUserCreationFactory.createUser("db_test_testuser1");
        user1 = entityManager.persistAndFlush(user1);

        user2 = TestUserCreationFactory.createUser("db_test_testuser2");
        user2 = entityManager.persistAndFlush(user2);
    }

    @Test
    public void findOrFailUserByEmailAddress_user_found_by_email_address() {
        /* execute */
        User user = userRepository.findOrFailUserByEmailAddress("db_test_testuser1@example.org");

        /* test */
        assertEquals(user1, user);
    }

    @Test
    public void findOrFailUserByEmailAddress_user_NOT_found_by_email_address() {
        /* execute */
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userRepository.findOrFailUserByEmailAddress("db_test_testuser_not_existing@example.org"));

        /* test */
        assertEquals("No user with email address 'db_test_testuser_not_existing@example.org' found!", exception.getMessage());
    }

    @Test
    public void user_being_owner_of_project_can_NOT_be_deleted__instead_a_notaccetable_exception_is_thrown() {
        /* test */
        expected.expect(NotAcceptableException.class);
        expected.expectMessage("still owner of a project");

        /* prepare */
        Project project = TestProjectCreationFactory.createProject("project_repo_test1", user1);

        entityManager.persist(project);

        /* execute */
        userRepository.deleteUserWithAssociations(user1.getName());

    }

    @Test
    public void user_being_owner_of_project_and_also_listed_as_user_can_NOT_be_deleted_instead_a_notacceptable_exception_is_thrown() {
        /* test */
        expected.expect(NotAcceptableException.class);
        expected.expectMessage("still owner of a project");

        /* prepare */
        Project project = TestProjectCreationFactory.createProject("project_repo_test2", user1);

        project.getUsers().add(user1);
        entityManager.persist(project);

        prepareForDBTest();

        /* execute */
        userRepository.deleteUserWithAssociations(user1.getName());

    }

    @Test
    public void user_NOT_being_owner_of_project_but_listed_as_user_can_be_deleted__and_project_is_NOT_deleted() throws Exception {
        /* prepare */
        Project project = TestProjectCreationFactory.createProject("project_repo_test3", user2);

        project.getUsers().add(user1);
        project.getWhiteList().add(new URI("http://www.example.org"));
        entityManager.persist(project);
        entityManager.persist(user1);

        prepareForDBTest();

        /* execute */
        userRepository.deleteUserWithAssociations(user1.getName());

        prepareForDBTest();
        assertProjectFound(project);

        assertUserNotFound(user1);

    }

    /**
     * This will flush data to DB and also clear entity manager, so cached objects
     * will be removed and we got fresh data from DB
     */
    private void prepareForDBTest() {
        entityManager.flush();
        entityManager.clear();
    }

    private void assertUserNotFound(User user) {
        Optional<User> userFound = userRepository.findById(user.getName());
        assertFalse(userFound.isPresent());
    }

    private void assertProjectFound(Project project) {
        Optional<Project> found = projectRepository.findById(project.getId());
        assertTrue(found.isPresent());
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
