package com.mercedesbenz.sechub.wrapper.prepare.upload;

public class PrepareWrapperUploadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private UploadExceptionExitCode exitCode;

    public PrepareWrapperUploadException(String message, UploadExceptionExitCode exitCode) {
        this(message, null, exitCode);
    }

    public PrepareWrapperUploadException(String message, Exception e, UploadExceptionExitCode exitCode) {
        super(message, e);
        this.exitCode = exitCode;
    }

    public UploadExceptionExitCode getExitCode() {
        return exitCode;
    }
}
