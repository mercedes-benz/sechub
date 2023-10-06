// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

public class User {

    private String userId;

    private String email;

    public User(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

}
