package com.mercedesbenz.sechub.systemtest.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.FailableRunnable;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;

public class SystemTestRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntime.class);

    private SystemTestRuntimePreparator preparator;

    private SystemTestRuntimeProductLauncher productLauncher;

    private SystemTestRuntimeHealthCheck healthCheck = new SystemTestRuntimeHealthCheck();

    private SystemTestRuntimeTestExecutor testsExecutor = new SystemTestRuntimeTestExecutor();

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

    public SystemTestResult run(SystemTestConfiguration configuration) {
        return run(configuration, true);
    }

    public SystemTestResult run(SystemTestConfiguration configuration, boolean localRun) {

        LOG.info("Starting - local run:{}", localRun);

        SystemTestRuntimeContext context = new SystemTestRuntimeContext(configuration, localRun);
        context.locationSupport = locationSupport;
        context.environmentProvider = environmentSupport;

        /* before tests */
        execOrFail(() -> preparator.prepare(context), "Preparation");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Configuration after prepare phase:\n{}", JSONConverter.get().toJSON(context.getConfiguration(), true));
        }

        execOrFail(() -> healthCheck.check(context), "Health check");

        execOrFail(() -> productLauncher.startSecHub(context), "Start SecHub");
        execOrFail(() -> productLauncher.startPDSSolutions(context), "Start PDS solutions");

        /* execute tests */
        testsExecutor.executeTests(context);

        /* after tests */
        execOrFail(() -> productLauncher.stopPDSSolutions(context), "Stop PDS solutions");
        execOrFail(() -> productLauncher.stopSecHub(context), "Stop SecHub");

        /* fetch results from context and publish only this */
        SystemTestResult result = new SystemTestResult();
        result.getRuns().addAll(context.getResults());

        return result;

    }

    private void execOrFail(FailableRunnable<Exception> runnable, String identifier) {
        try {
            runnable.runOrFail();
        } catch (WrongConfigurationException e) {
            String problem = identifier + " failed!\nReason:" + e.createDetails();
            LOG.error(problem);
            throw new RuntimeException(identifier + " failed: " + e.getMessage() + "\n(Look into log ouptput for more details)", e);
        } catch (Exception e) {
            String problem = identifier + " failed!\nReason:" + e.getMessage();
            LOG.error(problem);
            throw new RuntimeException(identifier + " failed: " + e.getMessage(), e);
        }
    }

}
