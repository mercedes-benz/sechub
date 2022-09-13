// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

public class ZapWrapperRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private ZapWrapperExitCode exitCode;

    public ZapWrapperRuntimeException(String message, ZapWrapperExitCode exitCode) {
        this(message, null, exitCode);
    }

    public ZapWrapperRuntimeException(String message, Throwable cause, ZapWrapperExitCode exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public ZapWrapperExitCode getExitCode() {
        return exitCode;
    }
}
