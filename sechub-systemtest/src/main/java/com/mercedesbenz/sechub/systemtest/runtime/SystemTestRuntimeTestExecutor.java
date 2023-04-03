package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestExecutionDefinition;

public class SystemTestRuntimeTestExecutor {

    public void executeTests(SystemTestRuntimeContext context) {
        testResults(context);
    }

    private void testResults(SystemTestRuntimeContext context) {

        List<TestDefinition> tests = context.getConfiguration().getTests();

        for (TestDefinition test : tests) {
            TestExecutionDefinition execute = test.getExecute();
            Optional<RunSecHubJobDefinition> runSecHubJobOpt = execute.getRunSecHubJob();
            if (!runSecHubJobOpt.isPresent()) {
                continue;
            }

            RunSecHubJobDefinition runSecHubJob = runSecHubJobOpt.get();
        }

    }
}
