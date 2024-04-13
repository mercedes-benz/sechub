// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.error;

import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionScope;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionState;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainer;

public class SystemTestScriptExecutionException extends SystemTestErrorException {

    private static final long serialVersionUID = 1L;

    public SystemTestScriptExecutionException(String scriptName, ProcessContainer procesContainer, SystemTestExecutionScope scope,
            SystemTestExecutionState state) {
        super(createExceptionMessage(scriptName, procesContainer, scope, state));

        String message = createMessage(scope, state);
        String details = createDetails(scriptName, procesContainer);

        defineError(message, details);
    }

    private static String createMessage(SystemTestExecutionScope scope, SystemTestExecutionState state) {
        return state + " " + scope + ": script failed";
    }

    private static String createDetails(String scriptName, ProcessContainer executionResult) {
        return "Script " + scriptName + " failed with exit code:" + executionResult.getExitValue() + "\nError message:" + executionResult.waitForErrorMessage();
    }

    private static String createExceptionMessage(String scriptName, ProcessContainer executionResult, SystemTestExecutionScope scope,
            SystemTestExecutionState state) {
        return createDetails(scriptName, executionResult);
    }
}
