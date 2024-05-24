package com.mercedesbenz.sechub.wrapper.prepare.modules;

public class PrepareWrapperInputValidatorException extends Exception {
    private static final long serialVersionUID = 1L;

    private InputValidatorExitcode exitCode;

    public PrepareWrapperInputValidatorException(String message, InputValidatorExitcode exitCode) {
        this(message, null, exitCode);
    }

    public PrepareWrapperInputValidatorException(String message, Exception e, InputValidatorExitcode exitCode) {
        super(message, e);
        this.exitCode = exitCode;
    }

    public InputValidatorExitcode getExitCode() {
        return exitCode;
    }
}
