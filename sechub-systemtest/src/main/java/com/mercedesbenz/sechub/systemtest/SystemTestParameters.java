// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest;

import java.util.LinkedHashSet;
import java.util.Set;

import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;

public class SystemTestParameters {

    private SystemTestConfiguration configuration;
    private String pathToPdsSolutionsRootFolder;
    private String pathToSechubSolutionRootFolder;
    private String pathToWorkspace;
    private String pathToAdditionalResources;
    private boolean localRun = true;// default always local
    private Set<String> testsToRun = new LinkedHashSet<>();

    private boolean dryRun;

    public SystemTestConfiguration getConfiguration() {
        return configuration;
    }

    public String getPathToPdsSolutionsRootFolder() {
        return pathToPdsSolutionsRootFolder;
    }

    public String getPathToSechubSolutionRootFolder() {
        return pathToSechubSolutionRootFolder;
    }

    /**
     * Returns a path to additional resources - e.g. checkout scripts etc.
     *
     * @return path
     */
    public String getPathToAdditionalResources() {
        return pathToAdditionalResources;
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

    public Set<String> getTestsToRun() {
        return testsToRun;
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
            parameter.pathToPdsSolutionsRootFolder = pathToPdsSolution;
            return this;
        }

        /**
         * You can use this method to define the SecHub solution path. But this is
         * normally not necessary. The location support will automatically use a
         * calculated server location from PDS solution path.
         *
         * @param pathToSecHubSolution
         * @return builder
         */
        public SystemTestParametersBuilder secHubSolutionPath(String pathToSecHubSolution) {
            parameter.pathToSechubSolutionRootFolder = pathToSecHubSolution;
            return this;
        }

        public SystemTestParametersBuilder workspacePath(String pathToWorkspace) {
            parameter.pathToWorkspace = pathToWorkspace;
            return this;
        }

        public SystemTestParametersBuilder additionalResourcesPath(String pathToAdditionalResources) {
            parameter.pathToAdditionalResources = pathToAdditionalResources;
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

        public SystemTestParametersBuilder testsToRun(String... testsToRun) {
            if (testsToRun == null) {
                return this;
            }
            for (String testToRun : testsToRun) {
                if (testToRun == null) {
                    continue;
                }
                parameter.testsToRun.add(testToRun);
            }
            return this;
        }

        public SystemTestParameters build() {
            return parameter;
        }
    }

}
