package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestExecutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.UploadDefinition;

/**
 * The main point when it comes to testing between SecHub and PDS solutions
 *
 * @author Albert Tregnaghi
 *
 */
public class SystemTestRuntimeTestEngine {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeTestEngine.class);

    private class SecHubRunData {

    }

    private class TestContext {

        private SecHubRunData secHubRunData;

        public boolean isSecHubTest() {
            return secHubRunData != null;
        }

        public SecHubRunData getSecHubRunData() {
            return secHubRunData;
        }

    }

    public void execute(TestDefinition test, SystemTestRuntimeContext context) {
        TestContext testContext = prepare(test, context);
        if (testContext.isSecHubTest()) {
//            launchSecHubJob(testContext.getSecHubRunData();
            /* FIXME Albert Tregnaghi, 2023-04-25:implement */
        } else {
            // currently we do only support SecHub runs
            throw new WrongConfigurationException("Cannot execute test: " + test.getName(), context);
        }

    }

    private void launchSecHubJob(SecHubRunData secHubRunData) {

    }

    private TestContext prepare(TestDefinition test, SystemTestRuntimeContext context) {
        TestContext testContext = new TestContext();
        TestExecutionDefinition execute = test.getExecute();
        Optional<RunSecHubJobDefinition> runSecHOptional = execute.getRunSecHubJob();
        if (runSecHOptional.isPresent()) {
            SecHubRunData secHubRunData = new SecHubRunData();
            RunSecHubJobDefinition runSecHubJobDefinition = runSecHOptional.get();

            UploadDefinition uploadDefinition = runSecHubJobDefinition.getUpload();

            testContext.secHubRunData = secHubRunData;
        }

        return testContext;
    }

    public void assertTestResults(TestDefinition test, SystemTestRuntimeContext context) {
        // TODO Auto-generated method stub

    }

}
