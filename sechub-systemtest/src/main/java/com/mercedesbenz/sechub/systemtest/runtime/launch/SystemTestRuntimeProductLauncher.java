package com.mercedesbenz.sechub.systemtest.runtime.launch;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.pdsclient.PDSClient;
import com.mercedesbenz.sechub.systemtest.pdsclient.PDSClientException;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionScope;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionState;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeException;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestErrorException;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestScriptExecutionException;

/**
 * The launcher class is responsible for start and stop of products (PDS
 * solutions, SecHub)
 *
 */
public class SystemTestRuntimeProductLauncher {

    private int maximumSecondsToWaitForSecHubAlive = 60;
    private int maximumSecondsToWaitForPDSSolutionAlive = 60;

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeProductLauncher.class);

    private ExecutionSupport execSupport;

    public SystemTestRuntimeProductLauncher(ExecutionSupport executionSupport) {
        this.execSupport = executionSupport;
    }

    public void startSecHub(SystemTestRuntimeContext context) throws SystemTestErrorException {
        if (!context.isLocalRun()) {
            LOG.debug("Skip sechub start - run is not local");
            return;
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        LocalSecHubDefinition localSecHub = localSetup.getSecHub();

        executeSteps("sechub", localSecHub.getStart(), context, SystemTestExecutionScope.SECHUB, SystemTestExecutionState.START);

        context.markSecHubStarted();
    }

    public void stopSecHub(SystemTestRuntimeContext context) throws SystemTestErrorException {
        if (!context.isLocalRun()) {
            LOG.debug("Skip sechub stop - run is not local");
            return;
        }
        if (!context.isSecHubStarted()) {
            LOG.debug("Skip sechub stop - no local sechub was started");
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        LocalSecHubDefinition localSecHub = localSetup.getSecHub();

        executeSteps("sechub", localSecHub.getStop(), context, SystemTestExecutionScope.SECHUB, SystemTestExecutionState.STOP);

    }

    public void startPDSSolutions(SystemTestRuntimeContext context) throws SystemTestErrorException {
        if (!context.isLocalRun()) {
            LOG.debug("Skip PDS solutions start - run is not local");
            return;
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();

        List<PDSSolutionDefinition> solutions = localSetup.getPdsSolutions();

        for (PDSSolutionDefinition solution : solutions) {
            executeSteps(solution.getName(), solution.getStart(), context, SystemTestExecutionScope.PDS_SOLUTION, SystemTestExecutionState.START);
        }
        context.markAtLeastOnePDSSolutionStarted();
    }

    public void stopPDSSolutions(SystemTestRuntimeContext context) throws SystemTestErrorException {
        if (!context.isLocalRun()) {
            LOG.debug("Skip PDS solutions stop - run is not local");
            return;
        }
        if (!context.isAtLeastOnePDSStarted()) {
            LOG.debug("Skip PDS solutions stop - no local PDS solution was started");
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        List<PDSSolutionDefinition> solutions = localSetup.getPdsSolutions();

        for (PDSSolutionDefinition solution : solutions) {

            executeSteps(solution.getName(), solution.getStop(), context, SystemTestExecutionScope.PDS_SOLUTION, SystemTestExecutionState.STOP);
        }

    }

    private void executeSteps(String name, List<ExecutionStepDefinition> steps, SystemTestRuntimeContext context, SystemTestExecutionScope scope,
            SystemTestExecutionState state) throws SystemTestScriptExecutionException {
        if (steps.isEmpty()) {
            LOG.debug("{} {}: {} - [Skipped because no steps defined]", state, scope, name);
            return;
        }
        LOG.debug("{} {}: {}", state, scope, name);

        for (ExecutionStepDefinition step : steps) {
            LOG.trace("Enter: {} - step: {}", name, step.getComment());
            if (step.getScript().isPresent()) {
                ScriptDefinition scriptDefinition = step.getScript().get();

                ProcessContainer processContainer = execSupport.execute(scriptDefinition);

                context.getCurrentStage().add(processContainer);

            }
        }
    }

    public void waitUntilSecHubAvailable(SystemTestRuntimeContext context) throws SystemTestErrorException {
        SecHubClient client = null;
        if (context.isLocalRun()) {
            if (!context.isLocalSecHubConfigured()) {
                /* not defined - no wait necessary */
                return;
            }
            LocalSecHubDefinition localSecHubDefinition = context.getLocalSecHubOrFail();
            Optional<Boolean> waitForAvailableOpt = localSecHubDefinition.getWaitForAvailable();
            if (waitForAvailableOpt.isPresent()) {

                if (!waitForAvailableOpt.get()) {
                    LOG.info("Do not wait for SecHub to become available because explicit not wished");
                    return;
                }
            }

            client = context.getLocalAdminSecHubClient();
        } else {
            if (!context.isRemoteSecHubConfigured()) {
                /* not defined - no wait necessary */
                return;
            }
            client = context.getRemoteUserSecHubClient();
        }
        if (context.isDryRun()) {
            LOG.info("Dry run: waitUntilSecHubAvailable is skipped");
            return;
        }
        try {
            long start = System.currentTimeMillis();
            LOG.info("Wait until SecHub server at {} is alive - will wait {} seconds.", client.getServerUri(), maximumSecondsToWaitForSecHubAlive);
            while (!client.checkIsServerAlive()) {
                Thread.sleep(1000);
                long millisecondsWaited = System.currentTimeMillis() - start;
                boolean timedOut = millisecondsWaited > maximumSecondsToWaitForSecHubAlive * 1000 * 1;
                if (timedOut) {
                    throw new IllegalStateException("Check for SecHub server alive timed out after " + (millisecondsWaited / 1000) + " seconds.");
                }
            }
            LOG.debug("SecHub server at {} is alive", client.getServerUri());

        } catch (SecHubClientException e) {
            throw new SystemTestRuntimeException("Was not able to check if SecHub server is alive.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void waitUntilPDSSolutionsAvailable(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            /* we do not wait for remote PDS solutions - must be available */
            return;
        }
        List<PDSSolutionDefinition> pdsSolutions = context.getLocalPdsSolutionsOrFail();
        for (PDSSolutionDefinition pdsSolution : pdsSolutions) {
            waitForPDSSolution(pdsSolution, context);
        }
    }

    private void waitForPDSSolution(PDSSolutionDefinition pdsSolution, SystemTestRuntimeContext context) {

        String pdsSolutionName = pdsSolution.getName();

        PDSClient client = context.getLocalTechUserPDSClient(pdsSolutionName);
        Optional<Boolean> waitForAvailableOpt = pdsSolution.getWaitForAvailable();
        if (waitForAvailableOpt.isPresent()) {

            if (!waitForAvailableOpt.get()) {
                LOG.info("Do not wait for PDS solution '" + pdsSolutionName + "' to become available because explicit not wished");
                return;
            }
        }

        client = context.getLocalTechUserPDSClient(pdsSolutionName);
        if (context.isDryRun()) {
            LOG.info("Dry run: waitUntilPDSavailable (pds solution '" + pdsSolutionName + "') is skipped");
            return;
        }
        try {
            long start = System.currentTimeMillis();
            LOG.info("Wait until PDS solution '" + pdsSolutionName + "' at {} is alive - will wait {} seconds.", client.getServerUri(),
                    maximumSecondsToWaitForPDSSolutionAlive);
            while (!client.checkIsServerAlive()) {
                Thread.sleep(1000);
                long millisecondsWaited = System.currentTimeMillis() - start;
                boolean timedOut = millisecondsWaited > maximumSecondsToWaitForPDSSolutionAlive * 1000 * 1;
                if (timedOut) {
                    throw new IllegalStateException(
                            "Check alive for PDS solution '" + pdsSolutionName + "' timed out after " + (millisecondsWaited / 1000) + " seconds.");
                }
            }
            LOG.debug("PDS solution '" + pdsSolutionName + "' at {} is alive", client.getServerUri());

        } catch (PDSClientException e) {
            throw new SystemTestRuntimeException("Was not able to check if PDS solution :'" + pdsSolutionName + "' is alive.", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

}