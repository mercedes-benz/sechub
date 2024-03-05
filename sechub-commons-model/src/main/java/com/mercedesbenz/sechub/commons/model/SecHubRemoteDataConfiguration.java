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

    /* Remote usage with user and password or certificate is currently not supported */
    /* We use our own PID users instead */

}
