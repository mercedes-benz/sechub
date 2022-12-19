// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.util.UUID;

public class AutoDumpPDSOutputForSecHubJobUUIDRunnable implements Runnable {

    private UUID sechubJobUUID;

    public AutoDumpPDSOutputForSecHubJobUUIDRunnable(UUID sechubJobUUID) {
        this.sechubJobUUID = sechubJobUUID;
    }

    @Override
    public void run() {
        TestAPI.dumpAllPDSJobOutputsForSecHubJob(sechubJobUUID);
    }

}