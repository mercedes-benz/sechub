// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

/**
 * Exception thrown when JSON validation fails
 */
public class JSONValidationException extends SecHubRuntimeException {

    private static final long serialVersionUID = 1L;

    public JSONValidationException(String message) {
        super(message);
    }

    public JSONValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
