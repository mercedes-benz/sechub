// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

public class PDSWorkspacePreparationResult {
    private boolean launcherScriptExecutable;

    public PDSWorkspacePreparationResult(boolean launcherScriptExecutable) {
        this.launcherScriptExecutable = launcherScriptExecutable;
    }

    public boolean isLauncherScriptExecutable() {
        return launcherScriptExecutable;
    }

    @Override
    public String toString() {
        return "PDSWorkspacePreparationResult [launcherScriptExecutable=" + launcherScriptExecutable + "]";
    }

}
