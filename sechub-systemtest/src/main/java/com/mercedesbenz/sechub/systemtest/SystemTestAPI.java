// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.systemtest.SystemTestParameters.SystemTestParametersBuilder;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfigurationBuilder;
import com.mercedesbenz.sechub.systemtest.runtime.LocationSupport;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntime;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeException;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ExecutionSupport;
import com.mercedesbenz.sechub.systemtest.runtime.variable.EnvironmentProvider;
import com.mercedesbenz.sechub.systemtest.runtime.variable.SystemEnvironmentProvider;

/**
 * This is the central point for system tests
 *
 * @author Albert Tregnaghi
 *
 */
public class SystemTestAPI {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestAPI.class);

    public static SystemTestConfigurationBuilder configure() {
        return SystemTestConfiguration.builder();
    }

    public static SystemTestParametersBuilder params() {
        return SystemTestParameters.builder();
    }

    public SystemTestResult runSystemTests(SystemTestParameters parameters) {
        return runSystemTests(parameters, null);
    }

    public SystemTestResult runSystemTests(SystemTestParameters parameters, EnvironmentProvider environmentSupport) {
        /* @formatter:off */
        LocationSupport locationSupport = LocationSupport.builder().

                pdsSolutionsRootFolder(parameters.getPathToPdsSolutionsRootFolder()).
                sechubSolutionRootFolder(parameters.getPathToSechubSolutionRootFolder()).
                additionalResourcesFolder(parameters.getPathToAdditionalResources()).
                workspaceRootFolder(parameters.getPathToWorkspace()).

                build();
        /* @formatter:on */

        cleanupOldRuntimeFolderIfExisting(locationSupport);
        if (environmentSupport == null) {
            environmentSupport = new SystemEnvironmentProvider();
        }
        ExecutionSupport execSupport = new ExecutionSupport(environmentSupport, locationSupport);

        SystemTestRuntime runtime = new SystemTestRuntime(locationSupport, execSupport);

        return runtime.run(parameters.getConfiguration(), parameters);
    }

    private void cleanupOldRuntimeFolderIfExisting(LocationSupport locationSupport) {
        Path runtimeFolder = locationSupport.getRuntimeFolder();
        if (Files.exists(runtimeFolder)) {
            try {
                PathUtils.delete(runtimeFolder);
                LOG.info("Deleted former existing runtime folder:{}", runtimeFolder);
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Was not able to delete former runtime folder:" + runtimeFolder, e);
            }
        }
    }

}
