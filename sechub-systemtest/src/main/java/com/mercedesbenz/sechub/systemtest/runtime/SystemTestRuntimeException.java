// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

public class SystemTestRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SystemTestRuntimeException(String message) {
        super(message);
    }

    public SystemTestRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
