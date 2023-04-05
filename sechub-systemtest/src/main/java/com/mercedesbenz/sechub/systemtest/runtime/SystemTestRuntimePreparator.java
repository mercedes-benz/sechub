package com.mercedesbenz.sechub.systemtest.runtime;

import static com.mercedesbenz.sechub.systemtest.config.DefaultFallback.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.ProjectDefinition;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubExecutorConfigDefinition;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.VariableConstants;
import com.mercedesbenz.sechub.systemtest.template.SystemTestTemplateEngine;

public class SystemTestRuntimePreparator {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimePreparator.class);
    private static final int MAX_VARIABLE_REPLACEMENT_LOOP_COUNT = 100;
    private SystemTestTemplateEngine templateEngine;

    public SystemTestRuntimePreparator() {
        this.templateEngine = new SystemTestTemplateEngine();
    }

    public void prepare(SystemTestRuntimeContext context) {

        initializeAlteredConfiguration(context);

        if (!context.isLocalRun()) {
            LOG.debug("Skip preparation - run is not local");
            return;
        }
        prepareLocal(context);
    }

    private void initializeAlteredConfiguration(SystemTestRuntimeContext context) {
        SystemTestConfiguration newConfiguration = createAlternativeConfigurationWithVariablesReplaced(context);

        context.alterConfguration(newConfiguration);
    }

    private SystemTestConfiguration createAlternativeConfigurationWithVariablesReplaced(SystemTestRuntimeContext context) {
        SystemTestConfiguration originConfiguration = context.getOriginConfiguration();
        String orgJson = JSONConverter.get().toJSON(originConfiguration);

        /* --------------------- */
        /* Environment variables */
        /* --------------------- */
        String alteredJson = templateEngine.replaceEnvironmentVariablesWithValues(orgJson, context.getEnvironmentProvider());
        /*
         * at this point we have no longer any data with ${env.XYZ} but only dedicated
         * data
         */
        if (templateEngine.hasEnvironmentVariables(alteredJson)) {
            throw new WrongConfigurationException("Cycle detected!\nWe do not allow environment variables which are nesting environment variables again!",
                    context);
        }

        /* -------------- */
        /* User variables */
        /* -------------- */
        int loopCount = 0;
        while (templateEngine.hasUserVariables(alteredJson)) {
            loopCount++;

            SystemTestConfiguration intermediateConfiguration = JSONConverter.get().fromJSON(SystemTestConfiguration.class, alteredJson);
            Map<String, String> variables = intermediateConfiguration.getVariables();

            if (loopCount > MAX_VARIABLE_REPLACEMENT_LOOP_COUNT) {
                throw new WrongConfigurationException("Cycle detected!\nA Lopp inside variable replacement has occurred.\nVariables currently:" + variables,
                        context);
            }

            alteredJson = templateEngine.replaceUserVariablesWithValues(alteredJson, variables);
        }

        /* ----------------- */
        /* Runtime variables */
        /* ----------------- */
        Map<String, String> runtimeVariables = createRuntimeVariables(context);
        alteredJson = templateEngine.replaceRuntimeVariablesWithValues(alteredJson, runtimeVariables);

        SystemTestConfiguration newConfiguration = JSONConverter.get().fromJSON(SystemTestConfiguration.class, alteredJson);
        return newConfiguration;
    }

    private Map<String, String> createRuntimeVariables(SystemTestRuntimeContext context) {
        Map<String, String> runtimeVariables = new LinkedHashMap<>();
        runtimeVariables.put(VariableConstants.VAR_WORKSPACE_ROOT, context.getWorkspaceRoot().toString());
        return runtimeVariables;
    }

    private void prepareLocal(SystemTestRuntimeContext context) {
        createDefaultsWhereNothingDefined(context);

        prepareScripts(context);
        collectLocalMetaData(context);

    }

    private void addFallbackDefaultProfileToExecutorsWithoutProfile(SecHubConfigurationDefinition sechubConfig) {
        List<SecHubExecutorConfigDefinition> executors = sechubConfig.getExecutors();
        for (SecHubExecutorConfigDefinition executor : executors) {
            String profile = executor.getProfile();
            if (profile == null || profile.isEmpty()) {
                executor.setProfile(FALLBACK_PROFILE_ID.getValue());
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
            fallback.setName(FALLBACK_PROJECT_NAME.getValue());
            fallback.setComment("Auto created fallback default project");
            fallback.getProfiles().add(FALLBACK_PROFILE_ID.getValue());
            projectsX.add(fallback);
        }
    }

    private void createDefaultsWhereNothingDefined(SystemTestRuntimeContext context) {
        /*
         * FIXME Albert Tregnaghi, 2023-03-24:we must introduce
         * "SechubDefaultsDefinition" + "PDSDefaultsDefinition". Here we will have only
         * a variant field (e.g "alpine") and at runtime, the model will be altered and
         * the start and stop objects are generated automatically by the default object.
         * So the default class must have the logic for the changes inside
         */
        SecHubConfigurationDefinition sechubConfig = context.getLocalSecHubConfigurationOrFail();

        /* setup default fallback implementations */
        createFallbackDefaultProjectWhenNoProjectsDefined(sechubConfig);
        addFallbackDefaultProfileToExecutorsWithoutProfile(sechubConfig);

    }

    private void prepareScripts(SystemTestRuntimeContext context) {
        prepareSecHubScripts(context);
        preparePDSSolutionScripts(context);
    }

    private void prepareSecHubScripts(SystemTestRuntimeContext context) {
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();

        LocalSecHubDefinition localSecHub = localSetup.getSecHub();

        Path sechubWorkingDirectory = resolveSecHubWorkingDirectory(localSecHub, context);
        prepareScriptData(localSecHub.getStart(), sechubWorkingDirectory);
        prepareScriptData(localSecHub.getStop(), sechubWorkingDirectory);
    }

    private void preparePDSSolutionScripts(SystemTestRuntimeContext context) {
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();

        List<PDSSolutionDefinition> solutions = localSetup.getPdsSolutions();

        for (PDSSolutionDefinition solution : solutions) {
            Path pdsWorkingDirectory = resolvePdsSolutionWorkingDirectory(solution, context);

            if (solution.getBaseDir() == null) {
                solution.setBaseDirectory(pdsWorkingDirectory.toString());
            }

            if (solution.getPathToPdsServerConfigFile() == null) {
                Path pdsServerConfigFilePath = pdsWorkingDirectory.resolve("docker/pds-config.json");
                solution.setPathToPdsServerConfigFile(pdsServerConfigFilePath.toString());
            }

            prepareScriptData(solution.getStart(), pdsWorkingDirectory);
            prepareScriptData(solution.getStop(), pdsWorkingDirectory);
        }
    }

    private void prepareScriptData(List<ExecutionStepDefinition> steps, Path workingDirectory) {
        for (ExecutionStepDefinition steep : steps) {
            if (steep.getScript().isPresent()) {
                ScriptDefinition scriptDefinition = steep.getScript().get();
                scriptDefinition.setWorkingDir(workingDirectory.toString());
            }
        }
    }

    private Path resolvePdsSolutionWorkingDirectory(PDSSolutionDefinition solution, SystemTestRuntimeContext context) {
        Path pdsSolutionsRootFolder = context.getLocationSupport().getPDSSolutionRoot();

        String pdsBaseDirAsString = solution.getBaseDir();
        if (pdsBaseDirAsString == null) {
            pdsBaseDirAsString = pdsSolutionsRootFolder.resolve(solution.getName()).toAbsolutePath().toString();
            LOG.debug("Base dir not set for pds solution:{}, so calculate base dir by name and root dir to:{}", solution.getName(), pdsBaseDirAsString);
        }
        Path pdsWorkingDirectory = Paths.get(pdsBaseDirAsString);
        return pdsWorkingDirectory;
    }

    private Path resolveSecHubWorkingDirectory(LocalSecHubDefinition localSecHub, SystemTestRuntimeContext context) {
        Path secHubWorkingDirectory = null;

        String sechubBaseDir = localSecHub.getBaseDir();
        if (sechubBaseDir != null && !sechubBaseDir.isEmpty()) {
            secHubWorkingDirectory = Paths.get(sechubBaseDir);
        } else {
            secHubWorkingDirectory = context.getLocationSupport().getSecHubSolutionRoot();
        }
        return secHubWorkingDirectory;
    }

    private void collectLocalMetaData(SystemTestRuntimeContext context) {
        SystemTestRuntimeMetaData runtimeMetaData = context.getRuntimeMetaData();
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        List<PDSSolutionDefinition> solutions = localSetup.getPdsSolutions();

        for (PDSSolutionDefinition solution : solutions) {
            runtimeMetaData.register(solution, context);
        }
    }
}
