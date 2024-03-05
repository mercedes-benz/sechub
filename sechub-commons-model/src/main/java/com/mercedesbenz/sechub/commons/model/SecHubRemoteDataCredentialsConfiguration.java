package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// TODO: 04.03.24 lbottne credentials im team klären

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataCredentialsConfiguration {
    public static final String PROPERTY_USER = "user";

    private SecHubRemoteDataUserConfiguration user;

    // TODO: 04.03.24 getter

}
