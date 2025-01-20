// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import java.time.LocalDateTime;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;

public class TestPDSJobHelper {

    /**
     * Creates a pds test job - started now, created 3 seconds before. State is
     * RUNNING
     *
     * @param uuid
     * @return job
     */
    public static final PDSJob createTestJobStartedNowCreated3SecondsBefore(UUID uuid) {
        /* UUID is not accessible outside package ... */

        PDSJob pdsJob = new PDSJob();
        pdsJob.uUID = uuid;
        pdsJob.created = LocalDateTime.now().minusSeconds(3);
        pdsJob.started = LocalDateTime.now();
        pdsJob.state = PDSJobStatusState.RUNNING;
        return pdsJob;
    }
}
