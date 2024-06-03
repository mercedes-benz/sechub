// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

public enum InputValidatorExitcode {

    LOCATION_NOT_MATCHING_PATTERN(1),

    CREDENTIALS_USERNAME_NOT_MATCHING_PATTERN(2),

    CREDENTIALS_PASSWORD_NOT_MATCHING_PATTERN(3),

    TYPE_NOT_MATCHING_PATTERN(4);

    private int exitCode;

    private InputValidatorExitcode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
