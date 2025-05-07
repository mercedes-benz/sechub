// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

public class TestUserCreationFactory {

    public static User createUser(String userId) {
        return createProjectUser(userId, false);
    }

    public static User createProjectUser(String userId, boolean superAdmin) {
        User user = new User();
        user.name = userId;
        user.emailAddress = userId + "@example.org";
        user.hashedApiToken = "12345678";
        user.superAdmin = superAdmin;
        return user;
    }
}
