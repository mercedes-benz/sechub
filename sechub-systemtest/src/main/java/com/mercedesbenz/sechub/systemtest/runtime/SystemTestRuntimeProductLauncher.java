package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        if (context.isLocalRun()) {
            LOG.debug("Skip sechub start - run is not local");
            return;
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        LocalSecHubDefinition localSecHub = localSetup.getSecHub();

        executeInWorkingDirectory("sechub", localSecHub.getBaseDir(), localSecHub.getStart(), context, SystemTestExecutionScope.SECHUB,
                SystemTestExecutionState.START);

    }

    public void stopSecHub(SystemTestRuntimeContext context) throws SystemTestErrorException {
        if (context.isLocalRun()) {
            LOG.debug("Skip sechub stop - run is not local");
            return;
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        LocalSecHubDefinition localSecHub = localSetup.getSecHub();

        executeInWorkingDirectory("sechub", localSecHub.getBaseDir(), localSecHub.getStop(), context, SystemTestExecutionScope.SECHUB,
                SystemTestExecutionState.STOP);

    }

    public void startPDSSolutions(SystemTestRuntimeContext context) throws SystemTestErrorException {
        if (context.isLocalRun()) {
            LOG.debug("Skip PDS solutions start - run is not local");
            return;
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();

        List<PDSSolutionDefinition> solutions = localSetup.getPdsSolutions();

        for (PDSSolutionDefinition solution : solutions) {
            executeInWorkingDirectory(solution.getName(), solution.getBaseDir(), solution.getStart(), context, SystemTestExecutionScope.PDS_SOLUTION,
                    SystemTestExecutionState.START);
        }

    }

    public void stopPDSSolutions(SystemTestRuntimeContext context) throws SystemTestErrorException {
        if (context.isLocalRun()) {
            LOG.debug("Skip PDS solutions stop - run is not local");
            return;
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        List<PDSSolutionDefinition> solutions = localSetup.getPdsSolutions();

        for (PDSSolutionDefinition solution : solutions) {

            executeInWorkingDirectory(solution.getName(), solution.getBaseDir(), solution.getStop(), context, SystemTestExecutionScope.PDS_SOLUTION,
                    SystemTestExecutionState.STOP);
        }

    }

    private void executeInWorkingDirectory(String name, String workingDirectoryAsString, List<ExecutionStepDefinition> startSteps,
            SystemTestRuntimeContext context, SystemTestExecutionScope scope, SystemTestExecutionState state) throws SystemTestScriptExecutionException {
        LOG.debug("{} {}: {}", state, scope, name);

        Path workingDirectory = Paths.get(workingDirectoryAsString);

        for (ExecutionStepDefinition startStep : startSteps) {
            LOG.trace("Enter: {} - step: {}", name, startStep.getComment());
            if (startStep.getScript().isPresent()) {
                ScriptDefinition scriptDefinition = startStep.getScript().get();
                executeScript(context, scriptDefinition, workingDirectory, scope, state);
            }
        }
    }

    private ExecutionResult executeScript(SystemTestRuntimeContext context, ScriptDefinition scriptDefinition, Path workingFolder,
            SystemTestExecutionScope scope, SystemTestExecutionState state) throws SystemTestScriptExecutionException {
        ExecutionResult executionResult;

        try {
            // we now set always the calculated working directory back into model
            // means we have now absolute pathes- easier to debug
            scriptDefinition.setWorkingDir(workingFolder.toString());
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
