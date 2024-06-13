package com.mercedesbenz.sechub.wrapper.prepare.modules;

public enum UsageExceptionExitCode {
    LOCATION_URL_NOT_VALID_URL(1),

    GIT_CLONING_FAILED(2),

    CREDENTIALS_NOT_DEFINED(3),

    USER_CREDENTIALS_NOT_DEFINED(4),

    CREDENTIAL_USER_NAME_NOT_DEFINED(5),

    CREDENTIAL_USER_PASSWORD_NOT_DEFINED(6),

    LOCATION_NOT_DEFINED(7),

    LOCATION_CONTAINS_FORBIDDEN_CHARACTER(8),

    DOWNLOAD_NOT_SUCCESSFUL(9),

    ONLY_ONE_REMOTE_DATA_CONFIGURATION_ALLOWED(10),

    ;

    private int exitCode;

    private UsageExceptionExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getCode() {
        return exitCode;
    }
}
