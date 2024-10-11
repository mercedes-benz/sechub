// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
/**
 * This registry is able to register sechub job runtime data thread safe
 *
 * @author Albert Tregnaghi
 *
 */
public class SchedulerSecHubJobRuntimeRegistry {

    private Object monitor = new Object();
    private Map<UUID, SchedulerSecHubJobRuntimeData> containers = new HashMap<>();

    public void register(SchedulerSecHubJobRuntimeData data) {
        if (data == null) {
            return;
        }
        UUID secHubJobUUID = data.getSecHubJobUUID();
        synchronized (monitor) {
            if (secHubJobUUID == null) {
                throw new IllegalArgumentException("sechub job uuid may not be null!");
            }
        }
        containers.put(secHubJobUUID, data);
    }

    public SchedulerSecHubJobRuntimeData fetchDataForSecHubJobUUID(UUID sechubJobUUID) {
        if (sechubJobUUID == null) {
            throw new IllegalArgumentException("sechub job uuid may not be null!");
        }
        synchronized (monitor) {
            return containers.get(sechubJobUUID);
        }
    }

    public void unregisterBySecHubJobUUID(UUID sechubJobUUID) {
        if (sechubJobUUID == null) {
            throw new IllegalArgumentException("sechub job uuid may not be null!");
        }
        synchronized (monitor) {
            containers.remove(sechubJobUUID);
        }
    }

    public Set<UUID> fetchAllSecHubJobUUIDs() {
        synchronized (monitor) {
            return containers.keySet();
        }
    }
}
