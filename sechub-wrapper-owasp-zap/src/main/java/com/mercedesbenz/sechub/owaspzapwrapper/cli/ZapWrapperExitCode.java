// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.cli;

/**
 *
 * Represents different kinds of exit codes, that can be used to determine why
 * the scan did exit with an error.
 *
 */
public enum ZapWrapperExitCode {

    SCAN_JOB_CANCELLED(0),

    PRODUCT_EXECUTION_ERROR(1),

    UNSUPPORTED_CONFIGURATION(2),

    PDS_CONFIGURATION_ERROR(3),

    TARGET_URL_INVALID(4),

    TARGET_URL_NOT_REACHABLE(5),

    API_DEFINITION_CONFIG_INVALID(6),

    IO_ERROR(7),

    ;

    private int exitCode;

    private ZapWrapperExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

}
