package com.mercedesbenz.sechub.xraywrapper.cli;

public enum XrayWrapperExitCode {

    ARTIFACTORY_NOT_REACHABLE(1),

    UNSUPPORTED_API_REQUEST(2),

    IO_ERROR(3),

    UNKNOWN_PARAMETERS(4),

    MALFORMED_URL(5),

    INVALID_HTTP_REQUEST(6),

    UNSUPPORTED_ENCODING(7),

    INVALID_JSON(8),

    ARTIFACT_NOT_FOUND(9),

    THREAD_INTERRUPTION(10),

    FILE_NOT_FOUND(11),

    FILE_NOT_VALID(12),

    UNSUPPORTED_SCAN_TYPE(13),

    ARTIFACTORY_ERROR_RESPONSE(14),

    INVALID_HTTP_RESPONSE(15),

    NOT_NULLABLE(16),

    ;

    private final int exitCode;

    XrayWrapperExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

}
