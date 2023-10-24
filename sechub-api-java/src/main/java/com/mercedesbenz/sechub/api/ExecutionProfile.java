// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class ExecutionProfile {

    private Set<String> projectIds = new TreeSet<>();

    private Set<ExecutorConfiguration> configurations = new HashSet<>();
    private String description;
    private boolean enabled;

    public Set<String> getProjectIds() {
        return projectIds;
    }

    public Set<ExecutorConfiguration> getConfigurations() {
        return configurations;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
