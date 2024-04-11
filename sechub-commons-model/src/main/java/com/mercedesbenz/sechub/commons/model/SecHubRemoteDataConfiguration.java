// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataConfiguration {

    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_CREDENTIALS = "credentials";

    private SecHubRemoteCredentialConfiguration credentials;

    private String type;

    private String location;

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public Optional<SecHubRemoteCredentialConfiguration> getCredentials() {
        return Optional.ofNullable(credentials);
    }

    public void setCredentials(SecHubRemoteCredentialConfiguration credentials) {
        this.credentials = credentials;
    }
}
