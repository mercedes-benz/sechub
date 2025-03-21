// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config.auth;

/**
 * Representing the different authentication types available to Zap. Makes the
 * Zap API easier to use.
 *
 */
public enum ZapAuthenticationType {

    UNAUTHENTICATED(null),

    HTTP_BASIC_AUTHENTICATION("httpAuthentication"),

    FORM_BASED_AUTHENTICATION("formBasedAuthentication"),

    SCRIPT_BASED_AUTHENTICATION("scriptBasedAuthentication"),

    JSON_BASED_AUTHENTICATION("jsonBasedAuthentication"),

    MANUAL_AUTHENTICATION("manualAuthentication"),

    ;

    private String zapAuthenticationMethod;

    private ZapAuthenticationType(String methodName) {
        this.zapAuthenticationMethod = methodName;
    }

    /**
     *
     * @return authentication method name or <code>null</code> in case of
     *         {@link #UNAUTHENTICATED}
     */
    public String getZapAuthenticationMethod() {
        return zapAuthenticationMethod;
    }
}
