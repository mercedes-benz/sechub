package com.mercedesbenz.sechub.integrationtest.api;

import java.util.UUID;

public class AutoDumpPDSOutputForPDSJobUUIDRunnable implements Runnable {

    private UUID pdsJobUUID;

    public AutoDumpPDSOutputForPDSJobUUIDRunnable(UUID pdsJobUUID) {
        this.pdsJobUUID = pdsJobUUID;
    }

    @Override
    public void run() {
        TestAPI.dumpPDSJobOutput(pdsJobUUID);
    }

}