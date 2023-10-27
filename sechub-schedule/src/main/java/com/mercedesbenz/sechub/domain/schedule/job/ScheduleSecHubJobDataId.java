// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ScheduleSecHubJobDataId implements Serializable {

    private static final long serialVersionUID = -5759402302814438404L;

    private UUID jobUUID;

    private String id;

    ScheduleSecHubJobDataId() {
        // for jpa only
    }

    public ScheduleSecHubJobDataId(UUID jobUUID, String id) {
        this.jobUUID = jobUUID;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public UUID getJobUUID() {
        return jobUUID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jobUUID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScheduleSecHubJobDataId other = (ScheduleSecHubJobDataId) obj;
        return Objects.equals(id, other.id) && Objects.equals(jobUUID, other.jobUUID);
    }

}
