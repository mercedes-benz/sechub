package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.systemtest.config.ConfigConstants;
import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.ProjectDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubExecutorConfigDefinition;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestExecutionDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestErrorException;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestExecutionScope;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestExecutionState;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestScriptExecutionException;

public class SystemTestRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntime.class);

    private ExecutionSupport execSupport;

    private LocationSupport locationSupport;

    public SystemTestRuntime(LocationSupport locationSupport, ExecutionSupport execSupport) {
        if (locationSupport == null) {
            throw new IllegalArgumentException("Location support may not be null!");
        }
        if (execSupport == null) {
            throw new IllegalArgumentException("Exec support may not be null!");
        }
        this.execSupport = execSupport;
        this.locationSupport = locationSupport;
    }

    public SystemTestResult run(SystemTestConfiguration configuration) {
        return run(configuration, true);
    }

    public SystemTestResult run(SystemTestConfiguration configuration, boolean localRun) {

        SystemTestRuntimeContext context = new SystemTestRuntimeContext(configuration, localRun);
        try {
            run("default", context);
        } catch (SystemTestErrorException e) {
            context.getCurrentResult().setError(e.getError());
        }

        SystemTestResult result = new SystemTestResult();
        result.getRuns().addAll(context.getResults());
        return result;
    }

    private void run(String runId, SystemTestRuntimeContext context) throws SystemTestErrorException {

        LOG.info("Run test: {}", runId);

        context.startNewRun(runId);

        /* preparation */
        if (context.isLocalRun()) {
            startLocalSecHubAndPDSSolutionInstances(context);

            configureLocalSecHub(context);
        }

        /* execute */
        executeScans(context);

        /* test */
        testResults(context);

        /* post parts */
        if (context.isLocalRun()) {
            stopLocalSecHubAndPDSSolutionInstances(context);
        }

    }

    private void startLocalSecHubAndPDSSolutionInstances(SystemTestRuntimeContext context) throws SystemTestScriptExecutionException {
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        LocalSecHubDefinition localSecHub = localSetup.getSecHub();

        Path secHubWorkingDirectory = resolveSecHubWorkingDirectory(localSecHub);

        /*
         * FIXME Albert Tregnaghi, 2023-03-24:we must introduce
         * "SechubDefaultsDefinition" + "PDSDefaultsDefinition". Here we will have only
         * a variant field (e.g "alpine") and at runtime, the model will be altered and
         * the start and stop objects are generated automatically by the default object.
         * So the default class must have the logic for the changes inside
         */

        /* start sechub */
        executeInWorkingDirectory("sechub", secHubWorkingDirectory, localSecHub.getStart(), context, SystemTestExecutionScope.SECHUB,
                SystemTestExecutionState.START);

        /* start solutions */
        List<PDSSolutionDefinition> solutions = localSetup.getPdsSolutions();

        Path pdsSolutionRootFolder = locationSupport.getPDSSolutionRootFolder();

        for (PDSSolutionDefinition solution : solutions) {
            Path pdsWorkingDirectory = resolvePdsSolutionWorkingDirectory(pdsSolutionRootFolder, solution);

            executeInWorkingDirectory(solution.getName(), pdsWorkingDirectory, solution.getStart(), context, SystemTestExecutionScope.PDS_SOLUTION,
                    SystemTestExecutionState.START);

            /* now create runtime data */

//            Pds
//             pdsWorkingDirectory.

        }

    }

    private Path resolvePdsSolutionWorkingDirectory(Path pdsRootFolder, PDSSolutionDefinition solution) {
        String pdsBaseDir = solution.getBaseDir();
        if (pdsBaseDir == null || pdsBaseDir.isEmpty()) {

            pdsBaseDir = pdsRootFolder.resolve(solution.getName()).toAbsolutePath().toString();
            LOG.debug("Base dir not set for pds solution:{}, so calculate base dir by name and root dir to:{}", solution.getName(), pdsBaseDir);
        }
        Path pdsWorkingDirectory = Paths.get(pdsBaseDir);
        return pdsWorkingDirectory;
    }

    private Path resolveSecHubWorkingDirectory(LocalSecHubDefinition localSecHub) {
        Path secHubWorkingDirectory = null;

        String sechubBaseDir = localSecHub.getBaseDir();
        if (sechubBaseDir != null && !sechubBaseDir.isEmpty()) {
            secHubWorkingDirectory = Paths.get(sechubBaseDir);
        } else {
            secHubWorkingDirectory = locationSupport.getSecHubSolutionRootFolder();
        }
        return secHubWorkingDirectory;
    }

    private void stopLocalSecHubAndPDSSolutionInstances(SystemTestRuntimeContext context) throws SystemTestErrorException {
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        List<PDSSolutionDefinition> solutions = localSetup.getPdsSolutions();
        Path pdsSolutionRootFolder = locationSupport.getPDSSolutionRootFolder();

        for (PDSSolutionDefinition solution : solutions) {
            Path pdsWorkingDirectory = resolvePdsSolutionWorkingDirectory(pdsSolutionRootFolder, solution);

            if (solution.getBaseDir() == null) {
                solution.setBaseDirectory(pdsWorkingDirectory.toString());
            }

            if (solution.getPathToPdsServerConfigFile() == null) {
                Path pdsServerConfigFilePath = pdsWorkingDirectory.resolve("docker/pds-config.json");
                solution.setPathToPdsServerConfigFile(pdsServerConfigFilePath.toString());
                if (!Files.exists(pdsServerConfigFilePath)) {
                    throw WrongConfigurationException.buildException(
                            "The calculated PDS server config file for solution:" + solution.getName() + " does not exist!\n" + "Calculated was: "
                                    + pdsServerConfigFilePath
                                    + "\nYou can set this manually by using 'pathToPdsServerConfigFile' at solution definition level.",
                            context.getConfiguration());
                }
            }

            executeInWorkingDirectory(solution.getName(), pdsWorkingDirectory, solution.getStop(), context, SystemTestExecutionScope.PDS_SOLUTION,
                    SystemTestExecutionState.STOP);
        }

        LocalSecHubDefinition localSecHub = localSetup.getSecHub();

        Path secHubWorkingDirectory = resolveSecHubWorkingDirectory(localSecHub);

        executeInWorkingDirectory("sechub", secHubWorkingDirectory, localSecHub.getStop(), context, SystemTestExecutionScope.SECHUB,
                SystemTestExecutionState.STOP);

    }

    private void testResults(SystemTestRuntimeContext context) throws SystemTestErrorException {

        List<TestDefinition> tests = context.getConfiguration().getTests();

        for (TestDefinition test : tests) {
            TestExecutionDefinition execute = test.getExecute();
            Optional<RunSecHubJobDefinition> runSecHubJobOpt = execute.getRunSecHubJob();
            if (!runSecHubJobOpt.isPresent()) {
                continue;
            }

            RunSecHubJobDefinition runSecHubJob = runSecHubJobOpt.get();
        }

    }

    private void executeScans(SystemTestRuntimeContext context) throws SystemTestErrorException {
        // TODO Auto-generated method stub

    }

    private void configureLocalSecHub(SystemTestRuntimeContext context) {
        SecHubConfigurationDefinition sechubConfig = context.getLocalSecHubConfigurationOrFail();

        createFallbackDefaultProjectWhenNoProjectsDefined(sechubConfig);
        addFallbackDefaultProfileToExecutorsWithoutProfile(sechubConfig);
    }

    private void addFallbackDefaultProfileToExecutorsWithoutProfile(SecHubConfigurationDefinition sechubConfig) {
        List<SecHubExecutorConfigDefinition> executors = sechubConfig.getExecutors();
        for (SecHubExecutorConfigDefinition executor : executors) {
            String profile = executor.getProfile();
            if (profile == null || profile.isEmpty()) {
                executor.setProfile(ConfigConstants.DEFAULT_PROFILE_ID);
            }
        }
    }

    private void createFallbackDefaultProjectWhenNoProjectsDefined(SecHubConfigurationDefinition sechubConfig) {
        Optional<List<ProjectDefinition>> projects = sechubConfig.getProjects();
        if (!projects.isPresent()) {
            sechubConfig.setProjects(Optional.of(new ArrayList<>()));
        }
        List<ProjectDefinition> projectsX = sechubConfig.getProjects().get();
        if (projectsX.isEmpty()) {
            ProjectDefinition fallback = new ProjectDefinition();
            fallback.setName(ConfigConstants.DEFAULT_PROJECT_NAME);
            fallback.setComment("Auto created fallback default project");
            fallback.getProfiles().add(ConfigConstants.DEFAULT_PROFILE_ID);
            projectsX.add(fallback);
        }
    }

    private void executeInWorkingDirectory(String name, Path workingDirectory, List<ExecutionStepDefinition> startSteps, SystemTestRuntimeContext context,
            SystemTestExecutionScope scope, SystemTestExecutionState state) throws SystemTestScriptExecutionException {
        LOG.debug("{} {}: {}", state, scope, name);

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
