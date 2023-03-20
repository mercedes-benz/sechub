package com.mercedesbenz.sechub.systemtest.runtime.error;

import com.mercedesbenz.sechub.systemtest.runtime.ExecutionResult;

public class SystemTestScriptExecutionException extends SystemTestErrorException {

    private static final long serialVersionUID = 1L;

    public SystemTestScriptExecutionException(String scriptName, ExecutionResult executionResult, SystemTestExecutionScope scope,
            SystemTestExecutionState state) {

        String message = state + " " + scope + ": script failed";
        String details = "Script " + scriptName + " failed with exit code" + executionResult.getExitValue() + "\nError message:"
                + executionResult.getErrorMessage();

        defineError(message, details);
    }
}
