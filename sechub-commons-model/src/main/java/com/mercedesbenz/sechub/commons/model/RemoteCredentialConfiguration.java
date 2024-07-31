// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT)
@MustBeKeptStable
public class RemoteCredentialConfiguration implements JSONable<RemoteCredentialConfiguration> {
    // TODO: 09.04.24 laura deprecated - decide if we keep credentials on PDS side
    public static final String PROPERTY_CREDENTIALS = "credentials";

    private static final RemoteCredentialConfiguration IMPORT = new RemoteCredentialConfiguration();

    private List<RemoteCredentialData> credentials = new ArrayList<>();

    public List<RemoteCredentialData> getCredentials() {
        return credentials;
    }

    @Override
    public Class<RemoteCredentialConfiguration> getJSONTargetClass() {
        return RemoteCredentialConfiguration.class;
    }

    public static final RemoteCredentialConfiguration fromJSONString(String json) {
        return IMPORT.fromJSON(json);
    }
}
