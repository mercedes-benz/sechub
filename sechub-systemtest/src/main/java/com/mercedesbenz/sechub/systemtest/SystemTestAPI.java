package com.mercedesbenz.sechub.systemtest;

import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfigurationBuilder;
import com.mercedesbenz.sechub.systemtest.runtime.EnvironmentProvider;
import com.mercedesbenz.sechub.systemtest.runtime.ExecutionSupport;
import com.mercedesbenz.sechub.systemtest.runtime.LocationSupport;
import com.mercedesbenz.sechub.systemtest.runtime.SystemEnvironmentProvider;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntime;

/**
 * This is the central point for system tests
 *
 * @author Albert Tregnaghi
 *
 */
public class SystemTestAPI {

    public static SystemTestConfigurationBuilder configure() {
        return SystemTestConfiguration.builder();
    }

    public static SystemTestResult runSystemTestsLocal(SystemTestConfiguration configuration, String pathToPdsSolution) {
        return runSystemTests(configuration, pathToPdsSolution, true);
    }

    public static SystemTestResult runSystemTestsRemote(SystemTestConfiguration configuration, String pathToPdsSolution) {
        return runSystemTests(configuration, pathToPdsSolution, false);
    }

    private static SystemTestResult runSystemTests(SystemTestConfiguration configuration, String pathToPdsSolution, boolean localRun) {
        LocationSupport locationSupport = new LocationSupport(pathToPdsSolution, null);

        EnvironmentProvider variableSupport = new SystemEnvironmentProvider();
        ExecutionSupport execSupport = new ExecutionSupport(variableSupport);

        SystemTestRuntime runtime = new SystemTestRuntime(locationSupport, execSupport);

        return runtime.run(configuration, localRun);
    }

}
