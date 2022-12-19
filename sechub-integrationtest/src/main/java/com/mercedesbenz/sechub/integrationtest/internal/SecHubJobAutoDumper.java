// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.UUID;
import java.util.concurrent.Callable;

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

    public <T> T execute(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            handleDumpingBeforeExceptionThrow();
            RuntimeException re = null;
            if (e instanceof RuntimeException) {
                re = (RuntimeException) e;
            } else {
                re = new RuntimeException("Wrapped origin exception:", e);
            }
            throw re;
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
