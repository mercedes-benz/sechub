package com.mercedesbenz.sechub.wrapper.prepare;

import com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode;

public class PrepareWrapperUsageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private UsageExceptionExitCode exitCode;

    public PrepareWrapperUsageException(String message, UsageExceptionExitCode exitCode) {
        this(message, null, exitCode);
    }

    public PrepareWrapperUsageException(String message, Exception e, UsageExceptionExitCode exitCode) {
        super(message, e);
        this.exitCode = exitCode;
    }

    public UsageExceptionExitCode getExitCode() {
        return exitCode;
    }
}
