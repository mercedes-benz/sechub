package com.mercedesbenz.sechub.systemtest;

import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;

public class SystemTestParameters {

    private SystemTestConfiguration configuration;
    private String pathToPdsSolution;
    private String pathToWorkspace;
    private boolean localRun = true;// default always local

    public SystemTestConfiguration getConfiguration() {
        return configuration;
    }

    public String getPathToPdsSolution() {
        return pathToPdsSolution;
    }

    public String getPathToWorkspace() {
        return pathToWorkspace;
    }

    public boolean isLocalRun() {
        return localRun;
    }

    public static SystemTestParametersBuilder builder() {
        return new SystemTestParametersBuilder();
    }

    public static class SystemTestParametersBuilder {
        SystemTestParameters parameter;

        private SystemTestParametersBuilder() {
            parameter = new SystemTestParameters();
        }

        public SystemTestParametersBuilder testConfiguration(SystemTestConfiguration configuration) {
            parameter.configuration = configuration;
            return this;
        }

        public SystemTestParametersBuilder pdsSolutionPath(String pathToPdsSolution) {
            parameter.pathToPdsSolution = pathToPdsSolution;
            return this;
        }

        public SystemTestParametersBuilder workspacePath(String pathToWorkspace) {
            parameter.pathToWorkspace = pathToWorkspace;
            return this;
        }

        public SystemTestParametersBuilder localRun() {
            parameter.localRun = true;
            return this;
        }

        public SystemTestParametersBuilder remoteRun() {
            parameter.localRun = false;
            return this;
        }

        SystemTestParameters build() {
            return parameter;
        }
    }

}
