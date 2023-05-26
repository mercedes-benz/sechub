package com.mercedesbenz.sechub.systemtest.runtime.test;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinitionTransformer;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionScope;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionState;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;
import com.mercedesbenz.sechub.systemtest.runtime.WrongConfigurationException;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestScriptExecutionException;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ExecutionSupport;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainer;

/**
 * The main point when it comes to testing between SecHub and PDS solutions
 *
 * @author Albert Tregnaghi
 *
 */
public class SystemTestRuntimeTestEngine {

    static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeTestEngine.class);

    private ExecutionSupport execSupport;

    RunSecHubJobDefinitionTransformer runSecHubJobTransformer = new RunSecHubJobDefinitionTransformer();

    public SystemTestRuntimeTestEngine(ExecutionSupport execSupport) {
        this.execSupport = execSupport;
    }

    public TestEngineContext createTestContext(TestDefinition test, SystemTestRuntimeContext context) {
        return new TestEngineContext(this, test, context);
    }

    public void execute(TestEngineContext testContext) {

        testContext.runtimeContext.testStarted(testContext.test);

        try {
            executePreparationSteps("Prepare", testContext);

        } catch (SystemTestScriptExecutionException e) {
            testContext.markAsFailed("Was not able to prepare test", e);
            return;
        }
        if (testContext.isSecHubTest()) {
            try {
                launchSecHubJob(testContext);
            } catch (SecHubClientException e) {
                testContext.markAsFailed("Was not able to launch SecHub job", e);
            }
        } else {
            // currently we do only support SecHub runs
            throw new WrongConfigurationException("Cannot execute test because not havign a sechub runs: " + testContext.test.getName(),
                    testContext.runtimeContext);
        }

    }

    private void launchSecHubJob(TestEngineContext testEngineContext) throws SecHubClientException {
        SecHubClient clientForScheduling = null;

        SystemTestRuntimeContext runtimeContext = testEngineContext.getRuntimeContext();
        if (runtimeContext.isLocalRun()) {
            clientForScheduling = runtimeContext.getLocalAdminSecHubClient();
        } else {
            clientForScheduling = runtimeContext.getRemoteUserSecHubClient();
        }
        SecHubConfigurationModel configuration = testEngineContext.getSecHubRunData().getSecHubConfiguration();

        UUID jobUUID = clientForScheduling.createJob(configuration);
        LOG.debug("SecHub job {} created", jobUUID);

    }

    private void executePreparationSteps(String name, TestEngineContext testContext) throws SystemTestScriptExecutionException {
        TestDefinition test = testContext.getTest();

        List<ExecutionStepDefinition> steps = test.getPrepare();
        if (steps.isEmpty()) {
            return;
        }

        for (ExecutionStepDefinition step : steps) {
            LOG.trace("Enter: {} - step: {}", name, step.getComment());
            if (step.getScript().isPresent()) {
                ScriptDefinition scriptDefinition = step.getScript().get();

                ProcessContainer processContainer = execSupport.execute(scriptDefinition, testContext.getDynamicVariableGenerator());
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

    class SecHubRunData {

        SecHubConfigurationModel secHubConfiguration;

        public SecHubConfigurationModel getSecHubConfiguration() {
            return secHubConfiguration;
        }

    }

}
