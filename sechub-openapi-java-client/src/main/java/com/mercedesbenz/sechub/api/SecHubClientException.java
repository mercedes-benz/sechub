// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

public class
SecHubClientException extends Exception {

    private static final long serialVersionUID = 1L;

    public SecHubClientException(String message) {
        super(message);
    }

    public SecHubClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
