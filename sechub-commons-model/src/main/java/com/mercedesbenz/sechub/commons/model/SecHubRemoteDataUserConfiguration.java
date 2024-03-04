package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataUserConfiguration {
    public static final String PROPERTY_REMOTE_NAME = "name";
    public static final String PROPERTY_REMOTE_PASSWORD = "password";

    private String user;

    private String password;

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
