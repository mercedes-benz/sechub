package com.mercedesbenz.sechub.xraywrapper.cli;

public enum XrayWrapperExitCode {

    ARTIFACTORY_NOT_REACHABLE(1),

    UNSUPPORTED_API_REQUEST(2),

    IO_ERROR(3),

    UNKNOWN_PARAMETERS(4),

    MALFORMED_URL(5),

    INVALID_HTTP_REQUEST(6),

    UNSUPPORTED_ENCRYPTION(7),

    JSON_NOT_PROCESSABLE(8),

    ARTIFACT_NOT_FOUND(9),

    ;

    private final int exitCode;

    XrayWrapperExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

}
