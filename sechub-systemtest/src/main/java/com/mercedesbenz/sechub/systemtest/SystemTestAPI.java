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

    public static SystemTestResult runSystemTests(SystemTestParameters parameters) {
        /* @formatter:off */
        LocationSupport locationSupport = LocationSupport.builder().

                pdsSolutionRootFolder(parameters.getPathToPdsSolution()).
                sechubSolutionRootFolder(parameters.getPathToSechubSolution()).
                additionalResourcesFolder(parameters.getPathToAdditionalResources()).
                workspaceRootFolder(parameters.getPathToWorkspace()).

                build();
        /* @formatter:on */

        cleanupOldRuntimeFolderIfExisting(locationSupport);

        EnvironmentProvider variableSupport = new SystemEnvironmentProvider();
        ExecutionSupport execSupport = new ExecutionSupport(variableSupport, locationSupport);

        SystemTestRuntime runtime = new SystemTestRuntime(locationSupport, execSupport);

        return runtime.run(parameters.getConfiguration(), parameters.isLocalRun(), parameters.isDryRun());
    }

    private static void cleanupOldRuntimeFolderIfExisting(LocationSupport locationSupport) {
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
