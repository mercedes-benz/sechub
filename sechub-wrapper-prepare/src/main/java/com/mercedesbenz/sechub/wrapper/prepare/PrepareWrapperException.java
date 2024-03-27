package com.mercedesbenz.sechub.wrapper.prepare;

public class PrepareWrapperException extends Exception {
    private static final long serialVersionUID = 1L;
    private final PrepareWrapperExitCode exitCode;

    public PrepareWrapperException(String message, PrepareWrapperExitCode exitCode) {
        this(message, exitCode, null);
    }

    public PrepareWrapperException(String message, PrepareWrapperExitCode exitCode, Throwable cause) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public PrepareWrapperExitCode getExitCode() {
        return exitCode;
    }
}
