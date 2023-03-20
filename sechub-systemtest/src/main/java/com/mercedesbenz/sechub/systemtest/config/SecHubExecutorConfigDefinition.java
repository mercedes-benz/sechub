package com.mercedesbenz.sechub.systemtest.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class SecHubExecutorConfigDefinition {

    private String profile;

    private String pdsProductId;

    private Map<String, String> parameters = new LinkedHashMap<>();

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setProfile(String profileId) {
        this.profile = profileId;
    }

    public String getProfile() {
        return profile;
    }

    public void setPdsProductId(String pdsProductId) {
        this.pdsProductId = pdsProductId;
    }

    public String getPdsProductId() {
        return pdsProductId;
    }
}
