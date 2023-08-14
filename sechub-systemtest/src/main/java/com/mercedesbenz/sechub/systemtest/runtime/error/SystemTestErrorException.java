// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.error;

public class SystemTestErrorException extends Exception {

    private static final long serialVersionUID = 1L;

    private SystemTestFailure error;

    public SystemTestErrorException(String message) {
        super(message);
    }

    protected void defineError(String message, String details) {
        error = new SystemTestFailure();
        error.setMessage(message);
        error.setDetails(details);
    }

    public SystemTestFailure getError() {
        return error;
    }

}
