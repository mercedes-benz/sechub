// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

/**
 *
 * Represents different kinds of exit codes, that can be used to determine why
 * the scan did exit with an error.
 *
 */
public enum ZapWrapperExitCode {

    PRODUCT_EXECUTION_ERROR(1),

    UNSUPPORTED_CONFIGURATION(2),

    PDS_CONFIGURATION_ERROR(3),

    TARGET_URL_INVALID(4),

    TARGET_URL_NOT_REACHABLE(5),

    API_DEFINITION_CONFIG_INVALID(6),

    IO_ERROR(7),

    SCAN_JOB_CANCELLED(8),

    INVALID_INCLUDE_OR_EXCLUDE_URLS(9),

    CLIENT_CERTIFICATE_CONFIG_INVALID(10),

    UNSUPPORTED_COMMANDLINE_CONFIGURATION(11),

    ;

    private int exitCode;

    private ZapWrapperExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

}
