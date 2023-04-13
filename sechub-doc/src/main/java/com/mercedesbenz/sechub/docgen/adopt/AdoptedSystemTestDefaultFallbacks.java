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
 * copy system test DefaultFallbacks at this location.
 *
 * @return
 */
public enum AdoptedSystemTestDefaultFallbacks {

    FALLBACK_PROJECT_NAME("Project name", "default-test-project"),

    FALLBACK_PROFILE_ID("Profile id", "default-test-profile"),;

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
