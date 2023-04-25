package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestExecutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.UploadDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestError;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestExecutionScope;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestExecutionState;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestScriptExecutionException;

/**
 * The main point when it comes to testing between SecHub and PDS solutions
 *
 * @author Albert Tregnaghi
 *
 */
public class SystemTestRuntimeTestEngine {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeTestEngine.class);

    private ExecutionSupport execSupport;

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

    public SystemTestRuntimeTestEngine(ExecutionSupport execSupport) {
        this.execSupport = execSupport;
    }

    public void execute(TestDefinition test, SystemTestRuntimeContext context) {
        try {

            context.startNewRun(test.getName());

            TestContext testContext = prepare(test, context);

            if (testContext.isSecHubTest()) {
                launchSecHubJob(testContext.getSecHubRunData());
            } else {
                // currently we do only support SecHub runs
                throw new WrongConfigurationException("Cannot execute test because not havign a sechub runs: " + test.getName(), context);
            }
        } catch (SystemTestScriptExecutionException e) {
            LOG.error("Test preparation failed for test: {}", test.getName(), e);

            SystemTestError error = new SystemTestError();
            error.setMessage("Was not able to prepare test : " + test.getName());
            error.setDetails(e.getMessage());

            context.getCurrentResult().setError(error);

        }

    }

    private void launchSecHubJob(SecHubRunData secHubRunData) {
        /* FIXME Albert Tregnaghi, 2023-04-25:implement */

    }

    private void executeSteps(String name, List<ExecutionStepDefinition> steps, SystemTestRuntimeContext context) throws SystemTestScriptExecutionException {
        if (steps.isEmpty()) {
            return;
        }

        for (ExecutionStepDefinition step : steps) {
            LOG.trace("Enter: {} - step: {}", name, step.getComment());
            if (step.getScript().isPresent()) {
                ScriptDefinition scriptDefinition = step.getScript().get();

                ProcessContainer processContainer = execSupport.execute(scriptDefinition);
                long startTime = System.currentTimeMillis();
                long diffTime = startTime;
                while (!processContainer.hasFailed() && processContainer.isStillRunning()) {
                    try {
                        long elapsedMilliseconds = System.currentTimeMillis() - diffTime;

                        if (elapsedMilliseconds > 5000) { // we log only every 5 seconds
                            diffTime = System.currentTimeMillis();
                            long secondsWaited = (System.currentTimeMillis() - startTime) / 1000;

                            LOG.info("Waiting now for test prepare script: {} - {} seconds waited at all", scriptDefinition.getPath(), secondsWaited);
                        }
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (processContainer.hasFailed()) {
                    throw new SystemTestScriptExecutionException(scriptDefinition.getPath(), processContainer, SystemTestExecutionScope.TEST,
                            SystemTestExecutionState.PREPARE);
                }

                if (processContainer.isTimedOut()) {
                    throw new SystemTestScriptExecutionException(scriptDefinition.getPath(), processContainer, SystemTestExecutionScope.TEST,
                            SystemTestExecutionState.PREPARE);
                }

            }
        }
    }

    private TestContext prepare(TestDefinition test, SystemTestRuntimeContext context) throws SystemTestScriptExecutionException {

        executeSteps("Prepare", test.getPrepare(), context);

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
