// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.UUID;

public class ExecutorConfigurationInfo {

    private UUID uuid;
    private String name;
    private boolean enabled;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
