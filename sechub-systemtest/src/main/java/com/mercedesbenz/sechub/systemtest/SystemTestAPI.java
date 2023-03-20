package com.mercedesbenz.sechub.systemtest;

import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfigurationBuilder;
import com.mercedesbenz.sechub.systemtest.runtime.ExecutionSupport;
import com.mercedesbenz.sechub.systemtest.runtime.LocationSupport;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntime;
import com.mercedesbenz.sechub.systemtest.runtime.VariableSupport;

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

    public static SystemTestResult runSystemTests(SystemTestConfiguration configuration, String pathToPdsSolution) {
        LocationSupport locationSupport = new LocationSupport(pathToPdsSolution, null);

        VariableSupport variableSupport = new VariableSupport();
        ExecutionSupport execSupport = new ExecutionSupport(variableSupport);

        SystemTestRuntime runtime = new SystemTestRuntime(locationSupport, execSupport);

        return runtime.run(configuration);
    }

}
