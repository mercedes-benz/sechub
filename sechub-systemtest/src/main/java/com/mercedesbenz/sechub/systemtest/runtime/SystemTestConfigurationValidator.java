package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestExecutionDefinition;

public class SystemTestConfigurationValidator {

    public void validate(SystemTestConfiguration configuration) {
        validateSecHubRunPossibleIfDefined(configuration);
    }

    private void validateSecHubRunPossibleIfDefined(SystemTestConfiguration configuration) {
        List<TestDefinition> tests = configuration.getTests();

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
