// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;

public class LocalSecHubDefinition extends AbstractSecHubDefinition {

    private CredentialsDefinition admin = new CredentialsDefinition();

    private List<ExecutionStepDefinition> start = new ArrayList<>();

    private SecHubConfigurationDefinition configure = new SecHubConfigurationDefinition();

    private List<ExecutionStepDefinition> stop = new ArrayList<>();

    private String baseDir;

    public SecHubConfigurationDefinition getConfigure() {
        return configure;
    }

    public List<ExecutionStepDefinition> getStart() {
        return start;
    }

    public List<ExecutionStepDefinition> getStop() {
        return stop;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public CredentialsDefinition getAdmin() {
        return admin;
    }

}
