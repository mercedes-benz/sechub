// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.cli.exceptions;

public class OpenAPITestToolRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OpenAPITestToolRuntimeException(String message) {
        this(message, null);
    }

    public OpenAPITestToolRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
