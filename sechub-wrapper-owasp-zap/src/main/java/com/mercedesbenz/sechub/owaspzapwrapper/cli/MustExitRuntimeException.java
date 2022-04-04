// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

public class MustExitRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private MustExitCode exitCode;

    public MustExitRuntimeException(String message, MustExitCode exitCode) {
        this(message, null, exitCode);
    }

    public MustExitRuntimeException(String message, Throwable cause, MustExitCode exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public MustExitCode getExitCode() {
        return exitCode;
    }
}
