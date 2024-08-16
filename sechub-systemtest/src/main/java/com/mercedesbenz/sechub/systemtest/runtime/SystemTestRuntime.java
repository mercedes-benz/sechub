// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.RunOrFail;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.SystemTestParameters;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.config.SystemTestRuntimeLocalSecHubProductConfigurator;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestErrorException;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestFailure;
import com.mercedesbenz.sechub.systemtest.runtime.init.SystemTestRuntimeContextHealthCheck;
import com.mercedesbenz.sechub.systemtest.runtime.init.SystemTestRuntimePreparator;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ExecutionSupport;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainer;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainerFailedException;
import com.mercedesbenz.sechub.systemtest.runtime.launch.SystemTestRuntimeProductLauncher;
import com.mercedesbenz.sechub.systemtest.runtime.testengine.SystemTestRuntimeTestEngine;
import com.mercedesbenz.sechub.systemtest.runtime.variable.EnvironmentProvider;

public class SystemTestRuntime {

    private static final int DEFAULT_SECONDS_TO_WAIT_FOR_REMAINIG_PROCESSES = 30;
    private static final int DEFAULT_SECONDS_TO_WAIT_FOR_STOP_SCRIPTS = 30;

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntime.class);

    private SystemTestRuntimePreparator preparator;

    private SystemTestRuntimeProductLauncher productLauncher;

    private SystemTestRuntimeContextHealthCheck healthCheck = new SystemTestRuntimeContextHealthCheck();

    private SystemTestRuntimeTestEngine testEngine;

    private LocationSupport locationSupport;

    private EnvironmentProvider environmentSupport;

    private SystemTestRuntimeLocalSecHubProductConfigurator localSecHubProductConfigurator;

    private int secondsToWaitForRemainingProcesses;

    private int secondsToWaitForStopScripts;

    public SystemTestRuntime(LocationSupport locationSupport, ExecutionSupport execSupport) {
        if (locationSupport == null) {
            throw new IllegalArgumentException("Location support may not be null!");
        }
        if (execSupport == null) {
            throw new IllegalArgumentException("Exec support may not be null!");
        }

        this.productLauncher = new SystemTestRuntimeProductLauncher(execSupport);
        this.localSecHubProductConfigurator = new SystemTestRuntimeLocalSecHubProductConfigurator();
        this.preparator = new SystemTestRuntimePreparator();
        this.locationSupport = locationSupport;
        this.environmentSupport = execSupport.getEnvironmentProvider();
        this.testEngine = new SystemTestRuntimeTestEngine(execSupport);

        this.secondsToWaitForStopScripts = getValue("SYSTEMTEST_WAIT_FOR_STOP_SCRIPTS", DEFAULT_SECONDS_TO_WAIT_FOR_STOP_SCRIPTS);
        this.secondsToWaitForRemainingProcesses = getValue("SYSTEMTEST_WAIT_FOR_REMAINING_PROCESSES", DEFAULT_SECONDS_TO_WAIT_FOR_REMAINIG_PROCESSES);
    }

    public SystemTestResult run(SystemTestConfiguration configuration, SystemTestParameters parameters) {

        SystemTestRuntimeContext context = initialize(configuration, parameters);

        return runAfterInitialization(context);

    }

    private int getValue(String key, int defaultValue) {
        String valueAsString = environmentSupport.getEnv(key);

        if (valueAsString == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(valueAsString);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("all")
    private SystemTestResult runAfterInitialization(SystemTestRuntimeContext context) {
        switchToStage("Setup", context);

        boolean stopSecHubTriggeredSuccessfully = false;
        boolean stopPDSTriggeredSuccessfully = false;

        SystemTestResult result = null;
        try {
            /* Setup phase 1 : SecHub */
            execOrFail(() -> productLauncher.startSecHub(context), "Start SecHub");
            execOrFail(() -> productLauncher.waitUntilSecHubAvailable(context), "Wait for SecHub available");
            execOrFail(() -> productLauncher.waitUntilSecHubAdminAvailable(context), "Wait for SecHub admin available");

            /*
             * Setup phase 2 : PDS solutions - it is ensured that SecHub has been started
             * (if common network is necessary)
             */
            execOrFail(() -> productLauncher.startPDSSolutions(context), "Start PDS solutions");
            execOrFail(() -> productLauncher.waitUntilPDSSolutionsAvailable(context), "Wait for PDS solutions available");

            /* Setup phase 3 : Configure SecHub */
            execOrFail(() -> localSecHubProductConfigurator.configure(context), "Apply SecHub configuration when local run");

            /* execute tests */
            switchToStage("Test", context);
            handleTests(context);

            /* shutdown */
            switchToStage("Shutdown", context);

            execOrFail(() -> productLauncher.stopPDSSolutions(context), "Stop PDS solutions");
            stopPDSTriggeredSuccessfully = true;

            execOrFail(() -> productLauncher.stopSecHub(context), "Stop SecHub");
            stopSecHubTriggeredSuccessfully = true;

            /* fetch results from context and publish only this */
            result = new SystemTestResult();
            result.getRuns().addAll(context.getResults());
            result.getProblems().addAll(context.getProblems());

            endCurrentStage(context);

            finalCheckForFailedContainers(context);

            return result;

        } finally {

            if (!stopPDSTriggeredSuccessfully) {
                try {
                    LOG.info("Trigger missing PDS solution stop");
                    productLauncher.stopPDSSolutions(context);
                } catch (SystemTestErrorException e) {
                    LOG.error("Retry to stop PDS solutions was not possible", e);
                }
            }
            if (!stopSecHubTriggeredSuccessfully) {
                try {
                    LOG.info("Trigger missing SecHub stop");
                    productLauncher.stopSecHub(context);
                } catch (SystemTestErrorException e) {
                    LOG.error("Retry to stop SecHub was not possible", e);
                }
            }

            terminateAndWaitForStillRunningProcesses(context);

            logResult(context, result);
        }
    }

    private void handleTests(SystemTestRuntimeContext context) {
        /* handle dynamic variables */
        List<TestDefinition> originTestList = context.getConfiguration().getTests();
        List<TestDefinition> workingList = new ArrayList<>(originTestList);
        originTestList.clear();

        boolean atLeastOneTestExecuted = false;
        Set<String> allExistingTestNames = new LinkedHashSet<>();
        for (TestDefinition test : workingList) {
            String testName = test.getName();

            allExistingTestNames.add(testName);

            if (context.isRunningTest(testName)) {
                atLeastOneTestExecuted = true;
                testEngine.runTest(test, context);
            }
        }
        if (!atLeastOneTestExecuted) {
            context.getProblems().add("No tests were executed (0/" + workingList.size() + ")");
        }

        if (!context.isRunningAllTests()) {
            /* check if the user wanted a test to run which does not exist */
            for (String testToRun : context.getTestsToRun()) {
                if (!allExistingTestNames.contains(testToRun)) {
                    SystemTestRunResult missingTestsResult = new SystemTestRunResult(testToRun);
                    missingTestsResult.setFailure(
                            new SystemTestFailure("Test '" + testToRun + "' is not defined!", "Following tests are defined: " + allExistingTestNames));
                    context.getResults().add(missingTestsResult);
                }
            }
        }
    }

    private void logResult(SystemTestRuntimeContext context, SystemTestResult result) {
        String resultStatus = "FAILED";
        int testsFailed = 0;
        int testsRun = 0;
        String failedTestsOutput = "";
        String problemsOutput = "";

        if (result != null) {
            if (!result.hasFailedTests()) {
                resultStatus = "SUCCESS";
            }
            testsFailed = result.getAmountOfFailedTests();
            testsRun = result.getAmountOfAllTests();

            if (result.hasProblems()) {

                StringBuilder sb = new StringBuilder();
                sb.append("Problems detected:\n");
                for (String problem : result.getProblems()) {
                    sb.append("  - ");
                    sb.append(problem);
                    sb.append("\n");
                }
                problemsOutput = sb.toString();
            }

        }

        if (testsFailed > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n  Failed tests:\n");
            for (SystemTestRunResult run : result.getRuns()) {
                if (run.hasFailed()) {
                    sb.append("   * '").append(run.getTestName()).append("' : ");
                    sb.append(run.getFailure().getMessage());
                    sb.append("\n");
                }
            }
            failedTestsOutput = sb.toString();
        }

        String info = """

                ------------------------------
                 System test result [%s]
                ------------------------------
                 - Tests run   : %d
                 - Tests failed: %d
                 %s
                 %s
                 Workspace: %s
                """.formatted(resultStatus, testsRun, testsFailed, failedTestsOutput, problemsOutput, context.getLocationSupport().getWorkspaceRoot());

        LOG.info(info);
    }

    private SystemTestRuntimeContext initialize(SystemTestConfiguration configuration, SystemTestParameters parameters) {
        SystemTestRuntimeContext context = new SystemTestRuntimeContext(configuration, locationSupport.getWorkspaceRoot(),
                locationSupport.getAdditionalResourcesRoot());

        context.localRun = parameters.isLocalRun();
        context.dryRun = parameters.isDryRun();
        context.addTestsToRun(parameters.getTestsToRun());

        LOG.info("Starting - run {}{}\nWorking directory: {}", context.dryRun ? "DRY " : "", context.localRun ? "LOCAL" : "REMOTE");

        context.locationSupport = locationSupport;
        context.environmentProvider = environmentSupport;

        switchToStage("Initialize context", context);

        /* before tests */
        execOrFail(() -> preparator.prepare(context), "Preparation");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Configuration after prepare phase:\n{}", JSONConverter.get().toJSON(context.getConfiguration(), true));
        }

        execOrFail(() -> healthCheck.check(context), "Health check");
        return context;
    }

    private void terminateAndWaitForStillRunningProcesses(SystemTestRuntimeContext context) {
        long startTimeStopScripts = System.currentTimeMillis();
        long timeToWaitForStopScriptsInMilliseconds = getSecondsToWaitForStopScripts() * 1000;

        // give stop scripts time to finish their job!
        for (SystemTestRuntimeStage stage : context.getStages()) {
            List<ProcessContainer> stillRunningProcessContainers = stage.getStillRunningContainers();
            for (ProcessContainer processContainer : stillRunningProcessContainers) {
                if (SystemTestExecutionState.STOP.equals(processContainer.getSystemTestExecutionState())) {
                    processContainer.waitForProcessTerminated(startTimeStopScripts, timeToWaitForStopScriptsInMilliseconds);
                }
            }
        }

        // terminate
        for (SystemTestRuntimeStage stage : context.getStages()) {
            List<ProcessContainer> stillRunningProcessContainers = stage.getStillRunningContainers();
            for (ProcessContainer processContainer : stillRunningProcessContainers) {
                processContainer.terminateProcess();
            }
        }

        // final, general wait for any processes - with time out
        long startTime = System.currentTimeMillis();
        int timeToWaitInMilliseconds = getSecondsToWaitForRemainingProcesses() * 1000;
        for (SystemTestRuntimeStage stage : context.getStages()) {
            List<ProcessContainer> stillRunningProcessContainers = stage.getStillRunningContainers();
            for (ProcessContainer processContainer : stillRunningProcessContainers) {
                processContainer.waitForProcessTerminated(startTime, timeToWaitInMilliseconds);
            }
        }
    }

    private int getSecondsToWaitForRemainingProcesses() {
        return secondsToWaitForRemainingProcesses;
    }

    private int getSecondsToWaitForStopScripts() {
        return secondsToWaitForStopScripts;
    }

    private void finalCheckForFailedContainers(SystemTestRuntimeContext context) {
        for (SystemTestRuntimeStage stage : context.getStages()) {
            List<ProcessContainer> failedContainers = stage.getFailedContainers();
            if (!failedContainers.isEmpty()) {
                throw new ProcessContainerFailedException(failedContainers.iterator().next());
            }
        }

    }

    private void switchToStage(String name, SystemTestRuntimeContext context) {
        /* end former stage */
        endCurrentStage(context);

        SystemTestRuntimeStage stage = new SystemTestRuntimeStage(name);
        context.setCurrentStage(stage);
        LOG.debug("Current stage now: {}", stage);
    }

    private void endCurrentStage(SystemTestRuntimeContext context) {
        SystemTestRuntimeStage stage = context.getCurrentStage();
        if (stage == null) {
            return;
        }
        LOG.debug("Wait for stage being ready to leave: {}", stage);
        while (!stage.isReadyToLeave()) {
            waitMilliseconds(300);
        }
    }

    private void waitMilliseconds(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void execOrFail(RunOrFail<Exception> runnable, String identifier) {
        try {
            runnable.runOrFail();
        } catch (WrongConfigurationException e) {
            String problem = identifier + " failed!\nReason: " + e.createDetails();
            LOG.error(problem);
            throw new SystemTestRuntimeException(identifier + " failed!\nReason: " + e.getMessage() + "\n(Look into log ouptput for more details)", e);

        } catch (SystemTestRuntimeException e) {
            String problem = identifier + " failed!\nReason: " + e.getMessage();
            LOG.error(problem);
            throw e;

        } catch (Exception e) {
            String problem = identifier + " failed!\nReason: " + e.getMessage();
            LOG.error(problem);

            throw new SystemTestRuntimeException(problem, e);
        }
    }

}
