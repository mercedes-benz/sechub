package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteCredentialUserData {

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PASSWORD = "password";

    private String name;

    private String password;

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
