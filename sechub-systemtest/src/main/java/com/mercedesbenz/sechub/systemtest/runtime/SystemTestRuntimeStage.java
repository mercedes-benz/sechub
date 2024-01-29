// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.systemtest.config.ProcessDefinition;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainer;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainerFailedException;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainerTimeOutException;

public class SystemTestRuntimeStage {

    private List<ProcessContainer> processContainers = new ArrayList<>();

    private String name;

    public SystemTestRuntimeStage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(ProcessContainer container) {
        processContainers.add(container);
    }

    /**
     * Checks if stage is ready to leave.
     *
     * @return <code>true</code> when ready to leave, <code>false</code> when not
     *
     * @throws ProcessContainerTimeOutException when at least one stage container
     *                                          has timed out
     * @throws ProcessContainerFailedException  when at least one stage container
     *                                          has failed
     */
    public boolean isReadyToLeave() {
        for (ProcessContainer container : processContainers) {
            if (container.isTimedOut()) {
                throw new ProcessContainerTimeOutException(container);
            }
            if (container.hasFailed()) {
                throw new ProcessContainerFailedException(container);
            }

            if (isWaitingFor(container)) {
                return false;
            }
        }
        return true;
    }

    private boolean isWaitingFor(ProcessContainer container) {
        ScriptDefinition scriptDefinition = container.getScriptDefinition();
        ProcessDefinition processDefinition = scriptDefinition.getProcess();

        boolean shallWait = processDefinition.isStageWaits();
        if (!shallWait) {
            return false;
        }
        return container.isStillRunning();

    }

    @Override
    public String toString() {
        return "SystemTestRuntimeStage [" + (name != null ? "name=" + name + ", " : "")
                + (processContainers != null ? "processContainers=" + processContainers : "") + "]";
    }

    public List<ProcessContainer> getFailedContainers() {

        List<ProcessContainer> failedContainers = new ArrayList<>();

        for (ProcessContainer container : processContainers) {
            if (container.hasFailed()) {
                failedContainers.add(container);
            }
        }
        return failedContainers;
    }

    public List<ProcessContainer> getStillRunningContainers() {
        List<ProcessContainer> stillRunningContainers = new ArrayList<>();

        for (ProcessContainer container : processContainers) {
            if (container.isStillRunning()) {
                stillRunningContainers.add(container);
            }
        }
        return stillRunningContainers;
    }
}
