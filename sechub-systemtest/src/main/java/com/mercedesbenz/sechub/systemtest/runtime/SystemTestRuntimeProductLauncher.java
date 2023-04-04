package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestErrorException;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestExecutionScope;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestExecutionState;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestScriptExecutionException;

/**
 * The launcher class is responsible for start and stop of products (PDS
 * solutions, SecHub)
 *
 */
public class SystemTestRuntimeProductLauncher {

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
                executeScript(context, scriptDefinition, scope, state);
            }
        }
    }

    private ExecutionResult executeScript(SystemTestRuntimeContext context, ScriptDefinition scriptDefinition, SystemTestExecutionScope scope,
            SystemTestExecutionState state) throws SystemTestScriptExecutionException {
        ExecutionResult executionResult;

        try {
            executionResult = execSupport.execute(scriptDefinition);

        } catch (IOException e) {
            String scriptPath = scriptDefinition.getPath();
            executionResult = new ExecutionResult();

            executionResult.exitValue = -1;
            executionResult.errorMessage = e.getMessage();

            LOG.warn("Script execution failed: {}", e.getMessage());

            throw new SystemTestScriptExecutionException(scriptPath, executionResult, scope, state);
        }
        if (executionResult.getExitValue() != 0) {
            String scriptPath = scriptDefinition.getPath();
            throw new SystemTestScriptExecutionException(scriptPath, executionResult, scope, state);
        }
        return executionResult;
    }

}
