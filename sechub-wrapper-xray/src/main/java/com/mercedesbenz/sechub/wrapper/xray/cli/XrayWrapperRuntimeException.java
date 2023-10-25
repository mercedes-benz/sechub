package com.mercedesbenz.sechub.wrapper.xray.cli;

public class XrayWrapperRuntimeException extends RuntimeException {

    private final XrayWrapperExitCode exitCode;

    public XrayWrapperRuntimeException(String message, XrayWrapperExitCode exitCode) {
        this(message, null, exitCode);
    }

    public XrayWrapperRuntimeException(String message, Throwable cause, XrayWrapperExitCode exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public XrayWrapperExitCode getExitCode() {
        return exitCode;
    }

}
