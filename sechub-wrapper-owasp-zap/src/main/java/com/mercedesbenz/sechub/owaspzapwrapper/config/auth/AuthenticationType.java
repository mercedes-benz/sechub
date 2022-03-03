// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.auth;

/**
 * Representing the different authentication types available in OWASP ZAP attack
 * proxy. Makes the OWASP ZAP API easier to use.
 *
 */
public enum AuthenticationType {

    UNAUTHENTICATED(null),

    HTTP_BASIC_AUTHENTICATION("httpAuthentication"),

    FORM_BASED_AUTHENTICATION("formBasedAuthentication"),

    SCRIPT_BASED_AUTHENTICATION("scriptBasedAuthentication"),

    JSON_BASED_AUTHENTICATION("jsonBasedAuthentication"),

    ;

    private String owaspZapAuthenticationMethod;

    private AuthenticationType(String methodName) {
        this.owaspZapAuthenticationMethod = methodName;
    }

    /**
     *
     * @return authentication method name or <code>null</code> in case of
     *         {@link #UNAUTHENTICATED}
     */
    public String getOwaspZapAuthenticationMethod() {
        return owaspZapAuthenticationMethod;
    }
}
