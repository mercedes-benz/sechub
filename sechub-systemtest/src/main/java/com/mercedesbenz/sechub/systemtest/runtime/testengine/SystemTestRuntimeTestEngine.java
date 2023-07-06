package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.api.SecHubReport;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinitionTransformer;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestExecutionDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.LocationSupport;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionScope;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionState;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeException;
import com.mercedesbenz.sechub.systemtest.runtime.WrongConfigurationException;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestFailure;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestScriptExecutionException;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ExecutionSupport;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainer;
import com.mercedesbenz.sechub.systemtest.runtime.variable.VariableCalculator;

/**
 * The main point when it comes to testing between SecHub and PDS solutions
 *
 * @author Albert Tregnaghi
 *
 */
public class SystemTestRuntimeTestEngine {

    static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeTestEngine.class);

    private static final long MILLISECONDS_TO_WAIT_FOR_JOB_FINISHED = 30 * 60_0000;

    private ExecutionSupport execSupport;

    RunSecHubJobDefinitionTransformer runSecHubJobTransformer = new RunSecHubJobDefinitionTransformer();

    public SystemTestRuntimeTestEngine(ExecutionSupport execSupport) {
        this.execSupport = execSupport;
    }

    public void execute(TestDefinition test, SystemTestRuntimeContext runtimeContext) {
        TestEngineTestContext testContext = new TestEngineTestContext(this, test, runtimeContext);

        testContext.runtimeContext.testStarted(testContext.test);

        try {
            executePreparationSteps("Prepare", testContext);

        } catch (SystemTestScriptExecutionException e) {
            testContext.markAsFailed("Was not able to prepare test", e);
            return;
        }

        if (testContext.isSecHubTest()) {
            try {
                testContext.markCurrentSecHubJob(launchSecHubJob(testContext));
            } catch (SecHubClientException e) {
                testContext.markAsFailed("Was not able to launch SecHub job", e);
            }
        } else {
            // currently we do only support SecHub runs
            throw new WrongConfigurationException("Cannot execute test: " + testContext.test.getName() + " because not found any sechub runs.",
                    testContext.runtimeContext);
        }

    }

    private UUID launchSecHubJob(TestEngineTestContext testContext) throws SecHubClientException {
        SecHubClient clientForScheduling = null;

        SystemTestRuntimeContext runtimeContext = testContext.getRuntimeContext();
        if (runtimeContext.isLocalRun()) {
            clientForScheduling = runtimeContext.getLocalAdminSecHubClient();
        } else {
            clientForScheduling = runtimeContext.getRemoteUserSecHubClient();
        }
        SecHubConfigurationModel configuration = testContext.getSecHubRunData().getSecHubConfiguration();

        UUID jobUUID = null;
        if (LOG.isTraceEnabled()) {
            LOG.trace("Start create job for sechub configuration:\n{}", JSONConverter.get().toJSON(configuration, true));
        }

        if (runtimeContext.isDryRun()) {
            jobUUID = UUID.randomUUID();
            LOG.debug("Skip job creation - use fake job uuid");
        } else {
            jobUUID = clientForScheduling.createJob(configuration);
        }
        LOG.debug("SecHub job {} created", jobUUID);

        /* we use the current test folder as working directory */
        Path workingDirectory = resolveWorkingDirectoryForCurrentTest(testContext);

        LOG.debug("Start upload job data. Use working directory of current test: {}", workingDirectory);
        String projectId = configuration.getProjectId();

        if (runtimeContext.isDryRun()) {
            LOG.debug("Skip job upload because dry run");
        } else {
            clientForScheduling.upload(projectId, jobUUID, configuration, workingDirectory);
        }

        /* mark job as ready to start */
        clientForScheduling.approveJob(projectId, jobUUID);

        /* wait for job failed or done */
        long started = System.currentTimeMillis();
        while (!clientForScheduling.fetchStatus(projectId, jobUUID).getResult().hasFinished()) {
            long diff = System.currentTimeMillis() - started;
            if (diff > MILLISECONDS_TO_WAIT_FOR_JOB_FINISHED) {
                throw new SystemTestRuntimeException("Job status for " + jobUUID + " took " + diff + " milliseconds (time out)");
            }
            try {
                TimeUnit.MICROSECONDS.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        SecHubReport report = clientForScheduling.downloadSecHubReportAsJson(projectId, jobUUID);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Report returned from SecHub server for job: {}:\n", report.toFormattedJSON());
        }

        return jobUUID;

    }

    private Path resolveWorkingDirectoryForCurrentTest(TestEngineTestContext testContext) {
        LocationSupport locationSupport = testContext.runtimeContext.getLocationSupport();
        Path workingDirectory = locationSupport.ensureTestWorkingDirectoryRealPath(testContext.getTest());

        return workingDirectory;
    }

    private void executePreparationSteps(String name, TestEngineTestContext testContext) throws SystemTestScriptExecutionException {
        TestDefinition test = testContext.getTest();

        List<ExecutionStepDefinition> steps = test.getPrepare();
        if (steps.isEmpty()) {
            return;
        }

        for (ExecutionStepDefinition step : steps) {
            LOG.trace("Enter: {} - step: {}", name, step.getComment());
            if (step.getScript().isPresent()) {
                executePreparationScript(testContext, step.getScript().get());
            }
        }
    }

    private void executePreparationScript(TestEngineTestContext testContext, ScriptDefinition scriptDefinition) throws SystemTestScriptExecutionException {

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

    class SecHubRunData {

        SecHubConfigurationModel secHubConfiguration;

        public SecHubConfigurationModel getSecHubConfiguration() {
            return secHubConfiguration;
        }

    }

    class TestEngineTestContext {

        private final SystemTestRuntimeTestEngine systemTestRuntimeTestEngine;
        private SecHubRunData secHubRunData;
        SystemTestRuntimeContext runtimeContext;
        TestDefinition test;
        private CurrentTestVariableCalculator dynamicVariableGenerator;

        TestEngineTestContext(SystemTestRuntimeTestEngine systemTestRuntimeTestEngine, TestDefinition test, SystemTestRuntimeContext runtimeContext) {
            this.systemTestRuntimeTestEngine = systemTestRuntimeTestEngine;
            this.test = test;
            this.runtimeContext = runtimeContext;
            this.dynamicVariableGenerator = new CurrentTestVariableCalculator(test, runtimeContext);

            appendSecHubRunData();
        }

        public VariableCalculator getDynamicVariableGenerator() {
            return dynamicVariableGenerator;
        }

        public TestDefinition getTest() {
            return test;
        }

        public boolean isSecHubTest() {
            return secHubRunData != null;
        }

        public SecHubRunData getSecHubRunData() {
            return secHubRunData;
        }

        public SystemTestRuntimeContext getRuntimeContext() {
            return runtimeContext;
        }

        private void appendSecHubRunData() {
            TestExecutionDefinition execute = test.getExecute();
            Optional<RunSecHubJobDefinition> runSecHOptional = execute.getRunSecHubJob();
            if (runSecHOptional.isEmpty()) {
                return;
            }
            JSONConverter converter = JSONConverter.get();

            secHubRunData = this.systemTestRuntimeTestEngine.new SecHubRunData();

            RunSecHubJobDefinition runSecHubJobDefinition = runSecHOptional.get();

            SecHubConfigurationModel secHubConfiguration = this.systemTestRuntimeTestEngine.runSecHubJobTransformer
                    .transformToSecHubConfiguration(runSecHubJobDefinition);

            String configurationAsJson = converter.toJSON(secHubConfiguration);

            String changedConfigurationAsJson = dynamicVariableGenerator.replace(configurationAsJson);

            secHubRunData.secHubConfiguration = converter.fromJSON(SecHubConfigurationModel.class, changedConfigurationAsJson);
            ;
        }

        public void markAsFailed(String message) {
            markAsFailed(message, null);
        }

        public void markAsFailed(String message, Exception e) {
            SystemTestRuntimeTestEngine.LOG.error("Test: {} failed: {}", getTest().getName(), message, e);

            SystemTestFailure failure = new SystemTestFailure();

            failure.setMessage("Test: " + getTest().getName() + " failed: " + message);
            failure.setDetails(createDetails(e));

            runtimeContext.getCurrentResult().setFailure(failure);
        }

        private String createDetails(Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("SecHubJob:").append(runtimeContext.getCurrentResult().getSechubJobUUID());
            sb.append(", ");
            if (e != null) {
                sb.append(e.getMessage());
            } else {
                sb.append("No exception available");
            }

            return sb.toString();
        }

        public void markCurrentSecHubJob(UUID sechubJobUUID) {
            runtimeContext.getCurrentResult().setSecHubJobUUID(sechubJobUUID);
        }

    }

}
