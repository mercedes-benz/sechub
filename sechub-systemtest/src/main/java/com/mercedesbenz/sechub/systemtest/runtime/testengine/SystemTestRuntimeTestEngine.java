// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubReport;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.systemtest.config.CopyDefinition;
import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinitionTransformer;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestAssertDefinition;
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
    SystemTestRuntimeTestAssertion testAssertion = new SystemTestRuntimeTestAssertion();
    CurrentTestVariableCalculatorFactory currentTestVariableCalculatorFactory = new DefaultCurrentTestVariableCalculatorFactory();

    public SystemTestRuntimeTestEngine(ExecutionSupport execSupport) {
        this.execSupport = execSupport;
    }

    public void runTest(TestDefinition test, SystemTestRuntimeContext runtimeContext) {
        TestEngineTestContext testContext = createTestContext(test, runtimeContext);

        prepareTest(testContext);

        executeTest(testContext);

        assertTest(testContext);

    }

    private TestEngineTestContext createTestContext(TestDefinition test, SystemTestRuntimeContext runtimeContext) {
        TestEngineTestContext testContext = new TestEngineTestContext(this, test, runtimeContext);
        testContext.runtimeContext.testStarted(testContext.test);
        return testContext;
    }

    private void assertTest(TestEngineTestContext testContext) {
        if (testContext.hasFailed()) {
            // already marked as failed
            return;
        }
        List<TestAssertDefinition> asserts = testContext.getTest().getAssert();
        for (TestAssertDefinition assertDef : asserts) {
            testAssertion.assertTest(assertDef, testContext);
        }
    }

    private void prepareTest(TestEngineTestContext testContext) {
        if (testContext.hasFailed()) {
            // already marked as failed
            return;
        }
        try {
            executePreparationSteps("Prepare", testContext);
        } catch (Exception e) {
            testContext.markAsFailed("Was not able to prepare test", e);
        }
    }

    private void executeTest(TestEngineTestContext testContext) {
        if (testContext.hasFailed()) {
            // already marked as failed
            return;
        }
        /* mark current test - fail if not supported */
        if (testContext.isSecHubTest()) {
            try {
                launchSecHubJob(testContext);
            } catch (Exception e) {
                testContext.markAsFailed("Was not able to launch SecHub job", e);
            }
        } else {
            // currently we do only support SecHub runs
            throw new WrongConfigurationException("Cannot execute test: " + testContext.test.getName() + " because not found any sechub runs.",
                    testContext.runtimeContext);
        }
    }

    private void launchSecHubJob(TestEngineTestContext testContext) throws Exception {
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
        testContext.getSecHubRunData().sechubJobUUID = jobUUID;

        /* we use the current test folder as working directory */
        Path workingDirectory = resolveWorkingDirectoryRealPathForCurrentTest(testContext);

        LOG.debug("Start upload job data. Use working directory of current test: {}", workingDirectory);
        String projectId = configuration.getProjectId();

        if (runtimeContext.isDryRun()) {
            LOG.debug("Skip job upload because dry run");
        } else {
            waitMilliseconds(300); // give server chance to create project parts
            clientForScheduling.upload(projectId, jobUUID, configuration, workingDirectory);
            waitMilliseconds(300); // give server chance to store result
        }

        /* mark job as ready to start */
        if (runtimeContext.isDryRun()) {
            LOG.debug("Skip job approve because dry run");
        } else {
            clientForScheduling.approveJob(projectId, jobUUID);
        }

        /* wait for job failed or done */
        if (runtimeContext.isDryRun()) {
            LOG.debug("Skip job status fetching because dry run");
        } else {
            long started = System.currentTimeMillis();
            while (!clientForScheduling.fetchJobStatus(projectId, jobUUID).getResult().hasFinished()) {
                long diff = System.currentTimeMillis() - started;
                if (diff > MILLISECONDS_TO_WAIT_FOR_JOB_FINISHED) {
                    throw new SystemTestRuntimeException("Job status for " + jobUUID + " took " + diff + " milliseconds (time out)");
                }
                waitMilliseconds(300);
            }
        }

        SecHubReport report = null;
        if (runtimeContext.isDryRun()) {
            LOG.debug("Simulate sechub report because dry run");
            report = new SecHubReport();
            report.setJobUUID(jobUUID);
        } else {
            report = clientForScheduling.downloadSecHubReportAsJson(projectId, jobUUID);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Report returned from SecHub server for job: {}:\n", report.toFormattedJSON());
        }
        testContext.getSecHubRunData().report = report;
        testContext.markCurrentSecHubJob(jobUUID);

    }

    private void waitMilliseconds(int milliSeconds) {
        try {
            TimeUnit.MICROSECONDS.sleep(milliSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    private Path resolveWorkingDirectoryRealPathForCurrentTest(TestEngineTestContext testContext) {
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
            if (step.getCopy().isPresent()) {
                executePreparationCopy(testContext, step.getCopy().get());
            }
            if (step.getScript().isPresent()) {
                executePreparationScript(testContext, step.getScript().get());
            }
        }
    }

    private void executePreparationCopy(TestEngineTestContext testContext, CopyDefinition copyDirectoriesDefinition) {
        String from = copyDirectoriesDefinition.getFrom();
        String to = copyDirectoriesDefinition.getTo();

        from = testContext.currentTestVariableCalculator.calculateValue(from);
        to = testContext.currentTestVariableCalculator.calculateValue(to);

        try {
            Path source = Paths.get(from).toRealPath();
            if (Files.isDirectory(source)) {
                Path destinationDirectory = Paths.get(to);
                destinationDirectory.toFile().mkdirs();

                PathUtils.copyDirectory(source, destinationDirectory);
            } else {
                Path destinationDirectory = Paths.get(to);
                destinationDirectory.toFile().mkdirs();
                PathUtils.copyFileToDirectory(source, destinationDirectory);
            }

        } catch (IOException e) {
            throw new WrongConfigurationException("Was not able to copy file/folders", testContext.runtimeContext, e);
        }
    }

    private void executePreparationScript(TestEngineTestContext testContext, ScriptDefinition scriptDefinition) throws SystemTestScriptExecutionException {

        ProcessContainer processContainer = execSupport.execute(scriptDefinition, testContext.getCurrentTestVariableCalculator(),
                SystemTestExecutionState.PREPARE);

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

        SecHubReport report;
        SecHubConfigurationModel secHubConfiguration;
        UUID sechubJobUUID;

        private SecHubRunData() {

        }

        public SecHubConfigurationModel getSecHubConfiguration() {
            return secHubConfiguration;
        }

        public SecHubReport getReport() {
            return report;
        }

        public UUID getSecHubJobUUID() {
            return sechubJobUUID;
        }

    }

    class DefaultCurrentTestVariableCalculatorFactory implements CurrentTestVariableCalculatorFactory {

        @Override
        public CurrentTestVariableCalculator create(TestDefinition test, SystemTestRuntimeContext runtimeContext) {
            return new CurrentTestVariableCalculator(test, runtimeContext);
        }

    }

    class TestEngineTestContext {

        private final SystemTestRuntimeTestEngine systemTestRuntimeTestEngine;
        private SecHubRunData secHubRunData;
        SystemTestRuntimeContext runtimeContext;
        TestDefinition test;
        private CurrentTestVariableCalculator currentTestVariableCalculator;

        TestEngineTestContext(SystemTestRuntimeTestEngine systemTestRuntimeTestEngine, TestDefinition test, SystemTestRuntimeContext runtimeContext) {
            this.systemTestRuntimeTestEngine = systemTestRuntimeTestEngine;
            this.test = test;
            this.runtimeContext = runtimeContext;
            this.currentTestVariableCalculator = currentTestVariableCalculatorFactory.create(test, runtimeContext);

            appendSecHubRunData();
        }

        public VariableCalculator getCurrentTestVariableCalculator() {
            return currentTestVariableCalculator;
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

            String changedConfigurationAsJson = currentTestVariableCalculator.replace(configurationAsJson);

            secHubRunData.secHubConfiguration = converter.fromJSON(SecHubConfigurationModel.class, changedConfigurationAsJson);
        }

        public void markAsFailed(String message, Exception e) {
            markAsFailed(message, null, e);
        }

        public void markAsFailed(String message, String hint) {
            markAsFailed(message, hint, null);
        }

        public void markAsFailed(String message, String hint, Exception e) {
            SystemTestRuntimeTestEngine.LOG.error("Test: {} failed: {}", getTest().getName(), message, e);

            SystemTestFailure failure = new SystemTestFailure();

            failure.setMessage(message);
            failure.setDetails(createDetails(hint, e));

            runtimeContext.getCurrentResult().setFailure(failure);
        }

        public boolean hasFailed() {
            return runtimeContext.getCurrentResult().hasFailed();
        }

        private String createDetails(String hint, Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n- SecHubJob UUID: ");
            sb.append(safeString(runtimeContext.getCurrentResult().getSechubJobUUID()));
            sb.append("\n- Hint: ");
            sb.append(safeString(hint));
            sb.append("\n- Exception: ");
            sb.append(safeString(e));

            return sb.toString();
        }

        public void markCurrentSecHubJob(UUID sechubJobUUID) {
            runtimeContext.getCurrentResult().setSecHubJobUUID(sechubJobUUID);
        }

        private String safeString(Object obj) {
            if (obj == null) {
                return "";
            }
            return obj.toString();
        }

    }

}
