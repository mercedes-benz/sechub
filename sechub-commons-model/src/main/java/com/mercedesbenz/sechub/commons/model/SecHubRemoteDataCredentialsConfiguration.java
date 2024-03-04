package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataCredentialsConfiguration {
    public static final String PROPERTY_USER = "user";

    private Optional<SecHubRemoteDataUserConfiguration> user = Optional.empty();

}
