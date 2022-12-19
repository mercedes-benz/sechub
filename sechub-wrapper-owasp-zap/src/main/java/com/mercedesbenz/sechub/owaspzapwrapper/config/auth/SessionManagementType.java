// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.auth;

/**
 * Representing the different session management types available to Owasp Zap.
 * Makes the OWASP ZAP API easier to use.
 *
 */
public enum SessionManagementType {

    HTTP_AUTH_SESSION_MANAGEMENT("httpAuthSessionManagement"),

    COOKIE_BASED_SESSION_MANAGEMENT("cookieBasedSessionManagement"),

    SCRIPT_BASED_SESSION_MANAGEMENT("scriptBasedSessionManagement"),

    ;

    private String owaspZapSessionManagementMethod;

    private SessionManagementType(String owaspZapSessionManagementMethod) {
        this.owaspZapSessionManagementMethod = owaspZapSessionManagementMethod;
    }

    public String getOwaspZapSessionManagementMethod() {
        return owaspZapSessionManagementMethod;
    }
}
