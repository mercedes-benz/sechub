package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataConfiguration {

    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_CREDENTIALS = "credentials";

    private String type;

    private String location;

    private SecHubRemoteDataCredentialsConfiguration credentials;

    public void setCredentials(SecHubRemoteDataCredentialsConfiguration credentials) {
        this.credentials = credentials;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public Optional<SecHubRemoteDataCredentialsConfiguration> getCredentials() {
        return Optional.ofNullable(credentials);
    }

}
