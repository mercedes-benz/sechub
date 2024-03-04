package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataContainer {

    public static final String PROPERTY_REMOTE = "remote";

    private Optional<SecHubRemoteDataConfiguration> remote = Optional.empty();

    public void setRemote(SecHubRemoteDataConfiguration remote) {
        this.remote = Optional.ofNullable(remote);
    }

    public Optional<SecHubRemoteDataConfiguration> getRemote() {
        return remote;
    }
}
