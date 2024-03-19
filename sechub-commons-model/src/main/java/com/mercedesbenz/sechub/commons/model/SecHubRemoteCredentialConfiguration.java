package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT)
@MustBeKeptStable
public class SecHubRemoteCredentialConfiguration implements JSONable<SecHubRemoteCredentialConfiguration> {

    public static final String PROPERTY_CREDENTIALS = "credentials";

    private static final SecHubRemoteCredentialConfiguration IMPORT = new SecHubRemoteCredentialConfiguration();

    private List<SecHubRemoteCredentialContainer> credentials = new ArrayList<>();

    public List<SecHubRemoteCredentialContainer> getCredentials() {
        return credentials;
    }

    @Override
    public Class<SecHubRemoteCredentialConfiguration> getJSONTargetClass() {
        return SecHubRemoteCredentialConfiguration.class;
    }

    public static final SecHubRemoteCredentialConfiguration fromJSONString(String json) {
        return IMPORT.fromJSON(json);
    }
}
