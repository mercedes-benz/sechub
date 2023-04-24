package com.mercedesbenz.sechub.systemtest.config;

/**
 * This class is used for default handling when user do not define their own
 * values.
 *
 * @author Albert Tregnaghi
 *
 */
public enum DefaultFallback {

    FALLBACK_PROJECT_NAME("Project name", "default-test-project"),

    FALLBACK_PROFILE_ID("Profile id", "default-test-profile"),

    /** Same like default in /sechub-solution/env-sechub */
    FALLBACK_SECHUB_LOCAL_URL("(Local) SecHub url", "https://localhost:8443"),

    /** Same like default in /sechub-solution/env-sechub */
    FALLBACK_SECHUB_ADMIN_USER("(Local) SecHub admin user", "admin"),

    /** Same like default in /sechub-solution/env-sechub */
    FALLBACK_SECHUB_ADMIN_TOKEN("(Local) SecHub admin token", "myTop$ecret!"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_PDS_ADMIN_USER("PDS admin user", "admin"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_PDS_ADMIN_TOKEN("PDS admin token", "pds-apitoken"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_PDS_TECH_USER("PDS tech user", "techuser"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_PDS_TECH_TOKEN("PDS tech user token", "pds-apitoken"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_PDS_LOCAL_URL("(Local) PDS url", "https://localhost:8444"),

    ;

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
