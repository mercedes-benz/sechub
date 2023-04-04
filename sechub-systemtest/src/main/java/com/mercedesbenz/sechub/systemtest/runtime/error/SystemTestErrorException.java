package com.mercedesbenz.sechub.systemtest.runtime.error;

public class SystemTestErrorException extends Exception {

    private static final long serialVersionUID = 1L;

    private SystemTestError error;

    public SystemTestErrorException(String message) {
        super(message);
    }

    protected void defineError(String message, String details) {
        error = new SystemTestError();
        error.setMessage(message);
        error.setDetails(details);
    }

    public SystemTestError getError() {
        return error;
    }

}
