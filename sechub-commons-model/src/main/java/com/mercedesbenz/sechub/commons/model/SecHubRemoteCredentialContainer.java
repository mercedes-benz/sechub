package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteCredentialContainer {

    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_PASSWORD = "password";
    public static final String PROPERTY_REMOTE_PATTERN = "remotePattern";
    public static final String PROPERTY_TYPE = "type";

    private String user;

    private String password;

    private String remotePattern;

    private String type;

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getRemotePattern() {
        return remotePattern;
    }

    public String getType() {
        return type;
    }
}
