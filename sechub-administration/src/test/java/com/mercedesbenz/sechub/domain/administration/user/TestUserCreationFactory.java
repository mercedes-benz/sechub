// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

public class TestUserCreationFactory {

    public static User createUser(String userId) {
        User user = new User();
        user.name = userId;
        user.emailAddress = userId + "@example.org";
        user.hashedApiToken = "12345678";
        return user;
    }
}
