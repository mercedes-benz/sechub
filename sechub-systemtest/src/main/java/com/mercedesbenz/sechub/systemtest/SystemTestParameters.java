package com.mercedesbenz.sechub.systemtest;

import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;

public class SystemTestParameters {

    private SystemTestConfiguration configuration;
    private String pathToPdsSolution;
    private String pathToWorkspace;
    private boolean localRun = true;// default always local

    private boolean dryRun;
    private String pdsServerconfigFileName = "pds-config.json";

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

    public boolean isDryRun() {
        return dryRun;
    }

    public String getPdsServerconfigFileName() {
        return pdsServerconfigFileName;
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

        public SystemTestParametersBuilder pdsServerConfigFileName(String pdsServerconfigFileName) {
            parameter.pdsServerconfigFileName = pdsServerconfigFileName;
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

        /**
         * Mark as "dry run" - will just start processes etc. but will not change
         * anything - only interesting for internal testing of the framework
         *
         * @return itself
         */
        public SystemTestParametersBuilder dryRun() {
            parameter.dryRun = true;
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
