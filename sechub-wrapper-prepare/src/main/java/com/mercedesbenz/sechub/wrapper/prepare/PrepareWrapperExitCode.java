package com.mercedesbenz.sechub.wrapper.prepare;

public enum PrepareWrapperExitCode {

    UNKNOWN_ERROR(1);

    private final int exitCode;

    PrepareWrapperExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
