// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver;

public final class ApplicationProfiles {

    public static final String BASIC_AUTH_MOCKED = "classic-auth-enabled";
    public static final String INTEGRATION_TEST_DATA = "integrationtest-data";
    public static final String LOCAL = "local";
    public static final String CLASSIC_AUTH_ENABLED = "classic-auth-enabled";
    public static final String OAUTH2_ENABLED = "oauth2";
    public static final String SSL_CERT_PROVIDED = "ssl-cert-provided";
    public static final String SSL_CERT_REQUIRED = "ssl-cert-required";
    public static final String TEST = "test";

    private ApplicationProfiles() {
        /* Prevent instantiation */
    }
}