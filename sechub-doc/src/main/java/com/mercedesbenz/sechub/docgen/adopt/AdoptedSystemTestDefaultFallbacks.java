package com.mercedesbenz.sechub.docgen.adopt;

/**
 * This class is necessary to avoid build cycles from system tests when
 * generating system test parts. <br>
 * <br>
 * Avoids cycle:
 *
 * <pre>
 * sechub-doc->restdoc tests->java compile necesary ->  generates openapi3.json
 * sechub-systemtest --> sechub-api-java --> openApiGenerator --> openapi3.json
 * </pre>
 *
 * Equality is checked by
 * <code>SystemTestFallbacksAndDocFallbacksTest.java</code> If it fails, please
 * copy content system test DefaultFallback at this location (class comments are
 * ignored means can be custom)
 *
 * @return
 */
public enum AdoptedSystemTestDefaultFallbacks {

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

    FALLBACK_PDS_LOCAL_URL("(Local) PDS url", "https://localhost:8444"),

    ;

    private String scope;
    private String value;

    AdoptedSystemTestDefaultFallbacks(String scope, String value) {
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
