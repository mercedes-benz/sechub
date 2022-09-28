// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.cli.exceptions;

public class SecHubClientConfigurationRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SecHubClientConfigurationRuntimeException(String message) {
        this(message, null);
    }

    public SecHubClientConfigurationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
