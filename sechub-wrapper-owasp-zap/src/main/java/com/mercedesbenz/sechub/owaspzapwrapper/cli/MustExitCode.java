// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

/**
 *
 * Represents different kinds of exit codes, that can be used to determine why
 * the scan did exit with an error.
 *
 */
public enum MustExitCode {

    EXECUTION_FAILED(1),

    ZAP_CONFIGURATION_INVALID(2),

    SECHUB_CONFIGURATION_INVALID(3),

    COMMANDLINE_CONFIGURATION_INVALID(4),

    TARGET_URL_CONFIGURATION_INVALID(5),

    AUTHENTICATIONTYPE_CONFIGURATION_INVALID(6),

    REPORT_FILE_ERROR(7),

    ;

    private int exitCode;

    private MustExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

}
