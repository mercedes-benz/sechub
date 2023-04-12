package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.FailableRunnable;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;

public class SystemTestRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntime.class);

    private SystemTestRuntimePreparator preparator;

    private SystemTestRuntimeProductLauncher productLauncher;

    private SystemTestRuntimeHealthCheck healthCheck = new SystemTestRuntimeHealthCheck();

    private SystemTestRuntimeTestExecutor testExecutor = new SystemTestRuntimeTestExecutor();
    private SystemTestRuntimeTestPreparator testPreparator = new SystemTestRuntimeTestPreparator();

    private LocationSupport locationSupport;

    private EnvironmentProvider environmentSupport;

    public SystemTestRuntime(LocationSupport locationSupport, ExecutionSupport execSupport) {
        if (locationSupport == null) {
            throw new IllegalArgumentException("Location support may not be null!");
        }
        if (execSupport == null) {
            throw new IllegalArgumentException("Exec support may not be null!");
        }
        this.productLauncher = new SystemTestRuntimeProductLauncher(execSupport);
        this.preparator = new SystemTestRuntimePreparator();
        this.locationSupport = locationSupport;
        this.environmentSupport = execSupport.getEnvironmentProvider();
    }

    public SystemTestResult run(SystemTestConfiguration configuration, boolean localRun) {

        SystemTestRuntimeContext context = new SystemTestRuntimeContext(configuration, locationSupport.getWorkspaceRoot(), localRun);
        try {
            LOG.info("Starting - local run:{}", localRun);

            context.locationSupport = locationSupport;
            context.environmentProvider = environmentSupport;

            switchToStage("Setup", context);

            /* before tests */
            execOrFail(() -> preparator.prepare(context), "Preparation");

            if (LOG.isDebugEnabled()) {
                LOG.debug("Configuration after prepare phase:\n{}", JSONConverter.get().toJSON(context.getConfiguration(), true));
            }

            execOrFail(() -> healthCheck.check(context), "Health check");

            execOrFail(() -> productLauncher.startSecHub(context), "Start SecHub");
            execOrFail(() -> productLauncher.startPDSSolutions(context), "Start PDS solutions");

            /* execute tests */
            switchToStage("Test", context);

            execOrFail(() -> prepareAndExecuteTests(context), "Start tests");

            execOrFail(() -> productLauncher.stopPDSSolutions(context), "Stop PDS solutions");
            execOrFail(() -> productLauncher.stopSecHub(context), "Stop SecHub");

            /* fetch results from context and publish only this */
            SystemTestResult result = new SystemTestResult();
            result.getRuns().addAll(context.getResults());

            endCurrentStage(context);

            finalCheckForFailedContainers(context);

            return result;
        } finally {
            for (SystemTestRuntimeStage stage : context.getStages()) {
                List<ProcessContainer> stillRunningProcessContainers = stage.getStillRunningContainers();
                for (ProcessContainer processContainer : stillRunningProcessContainers) {
                    processContainer.terminateProcess();
                }
            }
        }

    }

    private void finalCheckForFailedContainers(SystemTestRuntimeContext context) {
        for (SystemTestRuntimeStage stage : context.getStages()) {
            List<ProcessContainer> failedContainers = stage.getFailedContainers();
            if (!failedContainers.isEmpty()) {
                throw new ProcessContainerFailedException(failedContainers.iterator().next());
            }
        }

    }

    private void prepareAndExecuteTests(SystemTestRuntimeContext context) {
        List<TestDefinition> tests = context.getConfiguration().getTests();

        for (TestDefinition test : tests) {
            switchToStage("Test prepare", context);
            testPreparator.prepare(test);

            switchToStage("Test execution", context);
            testExecutor.executeTest(test);
        }

        /* after tests */
        switchToStage("Shutdown", context);
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

    private void execOrFail(FailableRunnable<Exception> runnable, String identifier) {
        try {
            runnable.runOrFail();
        } catch (WrongConfigurationException e) {
            String problem = identifier + " failed!\nReason:" + e.createDetails();
            LOG.error(problem);
            throw new SystemTestRuntimeException(identifier + " failed!\nReason: " + e.getMessage() + "\n(Look into log ouptput for more details)", e);
        } catch (Exception e) {
            String problem = identifier + " failed!\nReason:" + e.getMessage();
            LOG.error(problem);
            throw new SystemTestRuntimeException(identifier + " failed!\nReason: " + e.getMessage(), e);
        }
    }

}
