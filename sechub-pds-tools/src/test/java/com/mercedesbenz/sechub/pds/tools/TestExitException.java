package com.mercedesbenz.sechub.pds.tools;

class TestExitException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private int exitCode;

    public TestExitException(int exitCode) {
        super("ExitCode:" + exitCode);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}