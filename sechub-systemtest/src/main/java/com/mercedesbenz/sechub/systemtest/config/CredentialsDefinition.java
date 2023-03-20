package com.mercedesbenz.sechub.systemtest.config;

public class CredentialsDefinition extends AbstractDefinition {

    private String userId;

    private String apiToken;

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
