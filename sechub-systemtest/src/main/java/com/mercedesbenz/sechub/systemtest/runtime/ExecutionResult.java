package com.mercedesbenz.sechub.systemtest.runtime;

public class ExecutionResult {

    int exitValue;
    String errorMessage;
    String outputMessage;

    public int getExitValue() {
        return exitValue;
    }

    public String getOutputMessage() {
        return outputMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
