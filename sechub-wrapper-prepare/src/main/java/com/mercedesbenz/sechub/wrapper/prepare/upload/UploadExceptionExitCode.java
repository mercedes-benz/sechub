// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

public enum UploadExceptionExitCode {

    GIT_REPOSITORY_UPLOAD_FAILED(1),

    SKOPEO_BINARY_UPLOAD_FAILED(2),

    UNABLE_TO_STORE_FILE(3);

    private int exitCode;

    private UploadExceptionExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
