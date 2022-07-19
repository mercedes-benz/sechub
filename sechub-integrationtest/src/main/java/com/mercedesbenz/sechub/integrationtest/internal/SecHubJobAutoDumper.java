// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.UUID;

import com.mercedesbenz.sechub.integrationtest.api.TestAPI;

public class SecHubJobAutoDumper {

    private boolean autoDumpPDSjobsOfSecHubJobEnabled;
    private UUID sechubJobUUID;

    public void enablePDSAutoDumpOnErrorsForSecHubJob() {
        this.autoDumpPDSjobsOfSecHubJobEnabled = true;
    }

    public void execute(Runnable runnable) {
        try {
            runnable.run();
        } catch (AssertionError e) {
            handleDumpingBeforeExceptionThrow();
            throw e;
        }

    }

    private void handleDumpingBeforeExceptionThrow() {
        if (autoDumpPDSjobsOfSecHubJobEnabled) {
            TestAPI.dumpAllPDSJobOutputsForSecHubJob(sechubJobUUID);
        }

    }

    public void setSecHubJobUUID(UUID sechubJobUUID) {
        this.sechubJobUUID = sechubJobUUID;
    }
}
