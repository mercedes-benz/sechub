package com.mercedesbenz.sechub.domain.administration.user;

public class UserInfo {

    private final String userId;
    private final String email;

    public UserInfo(String userId, String email) {
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
