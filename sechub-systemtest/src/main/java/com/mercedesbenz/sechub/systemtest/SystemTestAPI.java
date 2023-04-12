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
import com.mercedesbenz.sechub.systemtest.runtime.EnvironmentProvider;
import com.mercedesbenz.sechub.systemtest.runtime.ExecutionSupport;
import com.mercedesbenz.sechub.systemtest.runtime.LocationSupport;
import com.mercedesbenz.sechub.systemtest.runtime.SystemEnvironmentProvider;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntime;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeException;

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

    public static SystemTestResult runSystemTests(SystemTestParameters parameter) {
        LocationSupport locationSupport = new LocationSupport(parameter.getPathToPdsSolution(), null, parameter.getPathToWorkspace());

        Path runtimeFolder = locationSupport.getRuntimeFolder();
        if (Files.exists(runtimeFolder)) {
            try {
                PathUtils.delete(runtimeFolder);
                LOG.info("Deleted former existing runtime folder:{}", runtimeFolder);
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Was not able to delete former runtime folder:" + runtimeFolder, e);
            }
        }

        EnvironmentProvider variableSupport = new SystemEnvironmentProvider();
        ExecutionSupport execSupport = new ExecutionSupport(variableSupport, locationSupport);

        SystemTestRuntime runtime = new SystemTestRuntime(locationSupport, execSupport);

        return runtime.run(parameter.getConfiguration(), parameter.isLocalRun());
    }

}
