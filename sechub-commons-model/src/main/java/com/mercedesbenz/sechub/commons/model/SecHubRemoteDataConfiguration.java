package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataConfiguration {

    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_LOCATION = "location";

    private String type;

    private String location;

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    /* Credentials implementation currently not supported

    public static final String PROPERTY_CREDENTIALS = "credentials";
    private SecHubRemoteDataCredentialsConfiguration credentials;

    public void setCredentials(SecHubRemoteDataCredentialsConfiguration credentials) {
        this.credentials = credentials;
    }

    public Optional<SecHubRemoteDataCredentialsConfiguration> getCredentials() {
        return Optional.ofNullable(credentials);
    }
     */

}
