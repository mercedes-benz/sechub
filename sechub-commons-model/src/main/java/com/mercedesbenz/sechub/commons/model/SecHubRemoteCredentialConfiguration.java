package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteCredentialConfiguration {
    public static final String PROPERTY_USER = "user";

    private String name;

    private SecHubRemoteCredentialUserData user;

    public void setUser(SecHubRemoteCredentialUserData user) {
        this.user = user;
    }

    public Optional<SecHubRemoteCredentialUserData> getUser() {
        return Optional.ofNullable(user);
    }

}
