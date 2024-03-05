package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// TODO: 04.03.24 lbottne credentials im team klären

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataCredentialsConfiguration {
    public static final String PROPERTY_USER = "user";

    private SecHubRemoteDataUserConfiguration user;

    public void setUser(SecHubRemoteDataUserConfiguration user) {
        this.user = user;
    }

    public Optional<SecHubRemoteDataUserConfiguration> getUser() {
        return Optional.ofNullable(user);
    }
}
