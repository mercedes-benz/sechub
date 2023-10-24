// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.UUID;

public class Job {

    private UUID uuid;
    private JobStatus status;

    public Job(UUID uuid) {
        this.uuid = uuid;
        this.status = new JobStatus();
    }

    public UUID getUuid() {
        return uuid;
    }

    public JobStatus getStatus() {
        return status;
    }

}
