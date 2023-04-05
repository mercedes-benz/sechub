package com.mercedesbenz.sechub.systemtest.config;

/**
 * This class is used for default handling when user do not define their own
 * values.
 *
 * @author Albert Tregnaghi
 *
 */
public enum DefaultFallback {

    FALLBACK_PROJECT_NAME("Project name", "default-test-project"), FALLBACK_PROFILE_ID("Profile id", "default-test-profile"),;

    private String scope;
    private String value;

    DefaultFallback(String scope, String value) {
        this.scope = scope;
        this.value = value;
    }

    public String getScope() {
        return scope;
    }

    public String getValue() {
        return value;
    }
}
