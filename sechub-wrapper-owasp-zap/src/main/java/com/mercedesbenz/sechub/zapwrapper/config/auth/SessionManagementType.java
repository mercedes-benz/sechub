// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config.auth;

/**
 * Representing the different session management types available to Zap. Makes
 * the ZAP API easier to use.
 *
 */
public enum SessionManagementType {

    HTTP_AUTH_SESSION_MANAGEMENT("httpAuthSessionManagement"),

    COOKIE_BASED_SESSION_MANAGEMENT("cookieBasedSessionManagement"),

    SCRIPT_BASED_SESSION_MANAGEMENT("scriptBasedSessionManagement"),

    ;

    private String zapSessionManagementMethod;

    private SessionManagementType(String zapSessionManagementMethod) {
        this.zapSessionManagementMethod = zapSessionManagementMethod;
    }

    public String getZapSessionManagementMethod() {
        return zapSessionManagementMethod;
    }
}
