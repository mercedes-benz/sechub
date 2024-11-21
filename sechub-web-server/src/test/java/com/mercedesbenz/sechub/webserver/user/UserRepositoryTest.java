package com.mercedesbenz.sechub.webserver.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
}