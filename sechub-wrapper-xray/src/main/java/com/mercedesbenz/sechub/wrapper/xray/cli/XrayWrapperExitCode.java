// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

public enum XrayWrapperExitCode {

    UNSUPPORTED_API_REQUEST(1),

    CREATE_CYCLONEDX_REPORT_ERROR(2),

    IO_ERROR(3),

    UNKNOWN_PARAMETERS(4),

    MALFORMED_URL(5),

    INVALID_HTTP_REQUEST(6),

    TIMEOUT_REACHED(7),

    INVALID_JSON(8),

    NOT_NULLABLE(9),

    THREAD_INTERRUPTION(10),

    ARTIFACTORY_ERROR_RESPONSE(11),

    INVALID_HTTP_RESPONSE(12),

    UNKNOWN_ERROR(13);

    private final int exitCode;

    XrayWrapperExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

}
