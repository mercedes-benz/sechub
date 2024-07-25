// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteCredentialData {

    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_PASSWORD = "password";
    public static final String PROPERTY_REMOTE_PATTERN = "remotePattern";
    public static final String PROPERTY_TYPES = "types";

    private String user;

    private String password;

    private String remotePattern;

    private final List<String> types = new ArrayList<>();

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    /**
     *
     * @return regular expression for the remote location
     */
    public String getRemotePattern() {
        return remotePattern;
    }

    public List<String> getTypes() {
        return types;
    }
}
