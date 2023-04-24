package com.mercedesbenz.sechub.systemtest.config;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SecHubExecutorConfigDefinition {

    private String pdsProductId;
    private int version = 1;

    private Map<String, String> parameters = new LinkedHashMap<>();

    private Set<String> profiles = new LinkedHashSet<>();
    private String baseURL;

    public Map<String, String> getParameters() {
        return parameters;
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
}
