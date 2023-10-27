// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SecHubConfigurationDefinition extends AbstractDefinition {

    private List<SecHubExecutorConfigDefinition> executors = new ArrayList<>();
    private Optional<List<ProjectDefinition>> projects = Optional.ofNullable(null);

    public List<SecHubExecutorConfigDefinition> getExecutors() {
        return executors;
    }

    // projects are normally not necessary. When not defined we will have a default
    // project up and running.
    public Optional<List<ProjectDefinition>> getProjects() {
        return projects;
    }

    public void setProjects(Optional<List<ProjectDefinition>> projects) {
        this.projects = projects;
    }
}
