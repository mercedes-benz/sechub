package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteCredentialData {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_USER = "user";

    private String name;

    private SecHubRemoteCredentialUserData user;

    public void setName(String uniqueName) {
        this.name = uniqueName;
    }

    public void setUser(SecHubRemoteCredentialUserData user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public Optional<SecHubRemoteCredentialUserData> getUser() {
        return Optional.ofNullable(user);
    }

}
