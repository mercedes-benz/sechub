// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.launch;

import com.mercedesbenz.sechub.systemtest.config.ProcessDefinition;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.TimeUnitDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeException;

public class ProcessContainerTimeOutException extends SystemTestRuntimeException {
    private static final long serialVersionUID = 1L;

    public ProcessContainerTimeOutException(ProcessContainer container) {
        super(createMessage(container), null);
    }

    private static String createMessage(ProcessContainer container) {
        ScriptDefinition script = container.getScriptDefinition();
        String path = script.getPath();
        ProcessDefinition process = script.getProcess();
        TimeUnitDefinition timeOut = process.getTimeOut();

        return "Container time out reached: " + timeOut.getAmount() + " " + timeOut.getUnit() + " for " + path;
    }

}
