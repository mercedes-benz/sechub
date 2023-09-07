// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SecHubExecutorConfigDefinition extends AbstractDefinition {

    private String pdsProductId;
    private String name;
    private int version;

    private Map<String, String> parameters = new LinkedHashMap<>();

    private Set<String> profiles = new LinkedHashSet<>();
    private String baseURL;
    private Optional<CredentialsDefinition> credentials = Optional.empty();

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfiles(Set<String> profileIds) {
        this.profiles.clear();
        this.profiles.addAll(profileIds);
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Set<String> getProfiles() {
        return profiles;
    }

    public void setPdsProductId(String pdsProductId) {
        this.pdsProductId = pdsProductId;
    }

    public String getPdsProductId() {
        return pdsProductId;
    }

    public int getVersion() {
        return version;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public Optional<CredentialsDefinition> getCredentials() {
        return credentials;
    }

    public void setCredentials(Optional<CredentialsDefinition> credentials) {
        this.credentials = credentials;
    }

}
