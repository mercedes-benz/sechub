// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.user;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("h2")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void assert_flyway_test_user_is_in_h2_database() {
        /* execute */
        List<User> users = userRepository.findAll();

        /* test */
        assert (users.size() == 1);
        assert (users.get(0).getName().equals("testuser"));
        assert (users.get(0).getEmailAddress().equals("testuser@example.com"));
        assert (users.get(0).getRoles().equals("USER"));
    }

    @Test
    void save_user_to_database() {
        /* prepare */
        User user = new User();
        user.setName("testuser2");
        user.setEmailAddress("tesuser2@mail.com");
        user.setRoles("USER");

        /* execute */
        userRepository.save(user);

        /* test */
        List<User> users = userRepository.findAll();
        assert (users.size() == 2);
    }

    @Test
    void find_user_from_database() {
        /* execute */
        Optional<User> optUser = userRepository.findById("testuser");

        /* test */
        assert (optUser.isPresent());
        User user = optUser.get();
        assert (user.getName().equals("testuser"));
        assert (user.getEmailAddress().equals("testuser@example.com"));
        assert (user.getRoles().equals("USER"));
    }

    @Test
    void save_and_delete_user_from_database() {
        /* prepare */
        Optional<User> optUser = userRepository.findById("testuser");
        assert (optUser.isPresent());
        User user = optUser.get();

        /* execute */
        userRepository.delete(user);

        /* test */
        List<User> users = userRepository.findAll();
        assert (users.isEmpty());
    }
}