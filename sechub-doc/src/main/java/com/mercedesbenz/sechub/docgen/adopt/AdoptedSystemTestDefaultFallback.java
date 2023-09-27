// SPDX-License-Identifier: MIT
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
 * <code>AdoptedSystemTestDefaultFallbacksTest.java</code> If it fails, please
 * copy content system test DefaultFallback at this location (class comments are
 * ignored means can be custom)
 *
 * @return
 */
public enum AdoptedSystemTestDefaultFallback {

    FALLBACK_PROJECT_NAME(StringConstants.DEFAULT_PROJECT_ID, "Project id"),

    FALLBACK_PROFILE_ID(StringConstants.DEFAULT_PROFILE_ID, "Profile id"),

    /** Same like default in /sechub-solution/env-sechub */
    FALLBACK_LOCAL_SECHUB_URL("https://localhost:8443", "Local", "SecHub url"),

    /** Same like default in /sechub-solution/env-sechub */
    FALLBACK_SECHUB_ADMIN_USER("admin", "SecHub admin user"),

    /** Same like default in /sechub-solution/env-sechub */
    FALLBACK_SECHUB_ADMIN_TOKEN("myTop$ecret!", "SecHub admin token"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_PDS_ADMIN_USER("admin", "PDS admin user"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_PDS_ADMIN_TOKEN("pds-apitoken", "PDS admin token"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_PDS_TECH_USER("techuser", "PDS tech user"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_PDS_TECH_TOKEN("pds-apitoken", "PDS tech user token"),

    /** Same like default in /sechub-pds-solutions/shared/environment/env-base */
    FALLBACK_LOCAL_PDS_URL("https://localhost:8444", "Local", "PDS url"),

    FALLBACK_LOCAL_PDS_WAIT_FOR_AVAILABLE("true", "Local", "PDS wait for available"),

    FALLBACK_SECHUB_WAIT_FOR_AVAILABLE("true", "SecHub wait for available"),

    FALLBACK_UPLOAD_REF_ID(StringConstants.DEFAULT_REFERENCE_ID, "Upload reference id"),

    ;

    private String scope;
    private String value;
    private String description;

    public class StringConstants {

        public static final String DEFAULT_PROJECT_ID = "default-test-project";
        public static final String DEFAULT_PROFILE_ID = "default-test-profile";
        public static final String DEFAULT_REFERENCE_ID = "default-ref";

    }

    AdoptedSystemTestDefaultFallback(String value, String description) {
        this(value, null, description);
    }

    AdoptedSystemTestDefaultFallback(String value, String scope, String description) {
        if (scope == null) {
            this.scope = "Global";
        } else {
            this.scope = scope;
        }
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getScope() {
        return scope;
    }

}
