package com.mercedesbenz.sechub.systemtest.runtime.error;

import com.mercedesbenz.sechub.systemtest.runtime.ExecutionResult;

public class SystemTestScriptExecutionException extends SystemTestErrorException {

    private static final long serialVersionUID = 1L;

    public SystemTestScriptExecutionException(String scriptName, ExecutionResult executionResult, SystemTestExecutionScope scope,
            SystemTestExecutionState state) {
        super(createExceptionMessage(scriptName, executionResult, scope, state));

        String message = createMessage(scope, state);
        String details = createDetails(scriptName, executionResult);

        defineError(message, details);
    }

    private static String createMessage(SystemTestExecutionScope scope, SystemTestExecutionState state) {
        return state + " " + scope + ": script failed";
    }

    private static String createDetails(String scriptName, ExecutionResult executionResult) {
        return "Script " + scriptName + " failed with exit code:" + executionResult.getExitValue() + "\nError message:" + executionResult.getErrorMessage();
    }

    private static String createExceptionMessage(String scriptName, ExecutionResult executionResult, SystemTestExecutionScope scope,
            SystemTestExecutionState state) {
        return createDetails(scriptName, executionResult);
    }
}
