// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.domain.administration.project.ProjectRepository;
import com.mercedesbenz.sechub.domain.administration.project.TestProjectCreationFactory;
import com.mercedesbenz.sechub.domain.administration.user.TestUserCreationFactory;
import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserRepository;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = { ProjectRepository.class, UserRepositoryDBTest.SimpleTestConfiguration.class })
public class UserRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;

    private User user2;

    @BeforeEach
    void before() {
        user1 = TestUserCreationFactory.createUser("db_test_testuser1");
        user1 = entityManager.persistAndFlush(user1);

        user2 = TestUserCreationFactory.createUser("db_test_testuser2");
        user2 = entityManager.persistAndFlush(user2);
    }

    @Test
    void findOrFailUserByEmailAddress_user_found_by_email_address() {
        /* execute */
        User user = userRepository.findOrFailUserByEmailAddress("db_test_testuser1@example.org");

        /* test */
        assertThat(user).isEqualTo(user1);
    }

    @Test
    void findOrFailUserByEmailAddress_user_NOT_found_by_email_address() {
        /* execute + test */
        assertThatThrownBy(() -> userRepository.findOrFailUserByEmailAddress("db_test_testuser_not_existing@example.org")).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No user with email address 'db_test_testuser_not_existing@example.org' found!");
    }

    @Test
    void user_being_owner_of_project_can_NOT_be_deleted__instead_a_notaccetable_exception_is_thrown() {
        /* prepare */
        Project project = TestProjectCreationFactory.createProject("project_repo_test1", user1);
        entityManager.persist(project);

        /* execute & test */
        assertThatThrownBy(() -> userRepository.deleteUserWithAssociations(user1.getName())).isInstanceOf(NotAcceptableException.class)
                .hasMessageContaining("db_test_testuser1").hasMessageContaining("1 times still owner");
    }

    @Test
    void user_being_owner_of_project_and_also_listed_as_user_can_NOT_be_deleted_instead_a_notacceptable_exception_is_thrown() {
        /* prepare */
        Project project = TestProjectCreationFactory.createProject("project_repo_test2", user1);

        project.getUsers().add(user1);
        entityManager.persist(project);

        prepareForDBTest();

        /* execute & test */
        assertThatThrownBy(() -> userRepository.deleteUserWithAssociations(user1.getName())).isInstanceOf(NotAcceptableException.class)
                .hasMessageContaining("db_test_testuser1 is 1 times still owner");
    }

    @Test
    void user_NOT_being_owner_of_project_but_listed_as_user_can_be_deleted__and_project_is_NOT_deleted() throws Exception {
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

    @Test
    void existsByEmailIgnoreCase_returns_false_when_email_not_in_database() {
        assertThat(userRepository.existsByEmailIgnoreCase("very_new_email@mail.com")).isFalse();
    }

    @Test
    void existsByEmailIgnoreCase_returns_true_when_email_exists_in_database() {
        /* prepare */
        String mail1 = user1.getEmailAddress();
        String mail2 = user2.getEmailAddress();
        String mail3 = mail1.toUpperCase();
        String mail4 = mail2.toUpperCase();
        String mail5 = mail1.replace("example", "exaMplE");
        assertThat(mail5.contains("exaMplE")).isTrue();

        /* execute + test */
        assertThat(userRepository.existsByEmailIgnoreCase(mail1)).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase(mail2)).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase(mail3)).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase(mail4)).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase(mail5)).isTrue();
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
        assertThat(userFound).isNotPresent();
    }

    private void assertProjectFound(Project project) {
        Optional<Project> found = projectRepository.findById(project.getId());
        assertThat(found).isPresent();
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}