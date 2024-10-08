// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.UUID;

public class SchedulerSecHubJobRuntimeData {

    private UUID secHubJobUUID;
    private UUID executionUUID;

    public SchedulerSecHubJobRuntimeData(UUID sechubJobUUID) {
        this.secHubJobUUID = sechubJobUUID;
    }

    public String getExecutionUUIDAsString() {
        return getUUIDAsString(executionUUID);
    }

    public String getSecHubJobUUIDasString() {
        return getUUIDAsString(secHubJobUUID);
    }

    public UUID getSecHubJobUUID() {
        return secHubJobUUID;
    }

    public void setSecHubJobUUID(UUID secHubJobUUID) {
        this.secHubJobUUID = secHubJobUUID;
    }

    public UUID getExecutionUUID() {
        return executionUUID;
    }

    public void setExecutionUUID(UUID executionUUID) {
        this.executionUUID = executionUUID;
    }

    private String getUUIDAsString(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return uuid.toString();
    }

}