package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.FailableRunnable;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.config.SystemTestRuntimeLocalSecHubProductConfigurator;
import com.mercedesbenz.sechub.systemtest.runtime.init.SystemTestRuntimeHealthCheck;
import com.mercedesbenz.sechub.systemtest.runtime.init.SystemTestRuntimePreparator;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ExecutionSupport;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainer;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainerFailedException;
import com.mercedesbenz.sechub.systemtest.runtime.launch.SystemTestRuntimeProductLauncher;
import com.mercedesbenz.sechub.systemtest.runtime.test.SystemTestRuntimeTestEngine;
import com.mercedesbenz.sechub.systemtest.runtime.test.TestEngineContext;
import com.mercedesbenz.sechub.systemtest.runtime.variable.EnvironmentProvider;

public class SystemTestRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntime.class);

    private SystemTestRuntimePreparator preparator;

    private SystemTestRuntimeProductLauncher productLauncher;

    private SystemTestRuntimeHealthCheck healthCheck = new SystemTestRuntimeHealthCheck();

    private SystemTestRuntimeTestEngine testEngine;

    private LocationSupport locationSupport;

    private EnvironmentProvider environmentSupport;

    private SystemTestRuntimeLocalSecHubProductConfigurator localSecHubProductConfigurator;

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
    }

    public SystemTestResult run(SystemTestConfiguration configuration, boolean localRun, boolean isDryRun) {

        SystemTestRuntimeContext context = new SystemTestRuntimeContext(configuration, locationSupport.getWorkspaceRoot(), localRun, isDryRun);
        try {
            LOG.info("Starting - run {}{}", isDryRun ? "DRY " : "", localRun ? "LOCAL" : "REMOTE");

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

            execOrFail(() -> productLauncher.waitUntilSecHubAvailable(context), "Wait for SecHub available");
            execOrFail(() -> productLauncher.waitUntilPDSSolutionsAvailable(context), "Wait for PDS solutions available");

            execOrFail(() -> localSecHubProductConfigurator.configure(context), "Apply SecHub configuration when local run");

            /* execute tests */
            switchToStage("Test", context);

            /* handle dynamic variables */
            List<TestDefinition> originTestList = context.getConfiguration().getTests();
            List<TestDefinition> workingList = new ArrayList<>(originTestList);
            originTestList.clear();

            for (TestDefinition test : workingList) {
                TestEngineContext testEngineContext = testEngine.createTestContext(test, context);
                testEngine.execute(testEngineContext);
            }

            /* shutdown */
            switchToStage("Shutdown", context);

            execOrFail(() -> productLauncher.stopPDSSolutions(context), "Stop PDS solutions");
            execOrFail(() -> productLauncher.stopSecHub(context), "Stop SecHub");

            /* fetch results from context and publish only this */
            SystemTestResult result = new SystemTestResult();
            result.getRuns().addAll(context.getResults());

            endCurrentStage(context);

            finalCheckForFailedContainers(context);

            return result;
        } finally {
            terminateStillRunningProcesses(context);
        }

    }

    private void terminateStillRunningProcesses(SystemTestRuntimeContext context) {
        for (SystemTestRuntimeStage stage : context.getStages()) {
            List<ProcessContainer> stillRunningProcessContainers = stage.getStillRunningContainers();
            for (ProcessContainer processContainer : stillRunningProcessContainers) {
                processContainer.terminateProcess();
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
