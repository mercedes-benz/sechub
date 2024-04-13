// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.launch;

import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeException;

public class ProcessContainerFailedException extends SystemTestRuntimeException {

    private static final long serialVersionUID = 1L;

    public ProcessContainerFailedException(ProcessContainer container) {
        super(createMessage(container), null);
    }

    private static String createMessage(ProcessContainer container) {
        ScriptDefinition script = container.getScriptDefinition();
        String path = script.getPath();

        return "Process container " + container.getUuid() + " failed.\nScript: " + path + "\nExit code: " + container.getExitValue() + "\nError message: "
                + container.waitForErrorMessage();
    }

}
