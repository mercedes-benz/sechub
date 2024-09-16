// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.init;

import static com.mercedesbenz.sechub.systemtest.config.DefaultFallback.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.internal.gen.model.*;
import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.systemtest.config.CalculatedVariables;
import com.mercedesbenz.sechub.systemtest.config.CredentialsDefinition;
import com.mercedesbenz.sechub.systemtest.config.DefaultFallback;
import com.mercedesbenz.sechub.systemtest.config.DefaultFallbackUtil;
import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.ProjectDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.RuntimeVariable;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubExecutorConfigDefinition;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.UploadDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeMetaData;
import com.mercedesbenz.sechub.systemtest.runtime.WrongConfigurationException;
import com.mercedesbenz.sechub.systemtest.template.SystemTestTemplateEngine;

/**
 * This class prepares / enhances the given system test configuration. By this
 * class it is possible to define only small parts inside the configuration file
 * and defaults or logical fallbacks are automatically applied.
 *
 * This means: We do not define defaults or fallback definitions inside the
 * model classes, but all default and fallback handling is done inside the
 * preparator class!
 *
 * @author Albert Tregnaghi
 *
 */
public class SystemTestRuntimePreparator {

    private static final String SUB_PATH_PDS_CONFIG_JSON = "docker/pds-config.json";
    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimePreparator.class);
    private static final int MAX_VARIABLE_REPLACEMENT_LOOP_COUNT = 100;

    public SystemTestRuntimePreparator() {

    }

    public void prepare(SystemTestRuntimeContext context) {

        initializeAlteredConfiguration(context);

        prepareLocal(context);
        prepareRemote(context);

        prepareTests(context);
    }

    private void prepareTests(SystemTestRuntimeContext context) {
        for (TestDefinition test : context.getConfiguration().getTests()) {
            prepareTest(test, context);
        }
    }

    private void prepareTest(TestDefinition test, SystemTestRuntimeContext context) {
        handleRunSecHubJob(test, context);
    }

    private void handleRunSecHubJob(TestDefinition test, SystemTestRuntimeContext context) {
        Optional<RunSecHubJobDefinition> runSecHubJobOptional = test.getExecute().getRunSecHubJob();
        if (runSecHubJobOptional.isEmpty()) {
            return;
        }
        RunSecHubJobDefinition runSecHubJob = runSecHubJobOptional.get();
        String project = runSecHubJob.getProject();
        if (SimpleStringUtils.isEmpty(project)) {
            runSecHubJob.setProject(DefaultFallback.FALLBACK_PROJECT_NAME.getValue());
        }
        handleCodeScan(test, context, runSecHubJob);
        handleSecretScan(test, context, runSecHubJob);
        handleLicenseScan(test, context, runSecHubJob);
        handleWebScan(test, context, runSecHubJob);
        handleInfraScan(test, context, runSecHubJob);

        handleUploads(test, context, runSecHubJob);
    }

    private void handleUploads(TestDefinition test, SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        List<UploadDefinition> uploads = runSecHubJob.getUploads();
        for (UploadDefinition upload : uploads) {
            handleMissingUploadReferenceId(upload);
        }
    }

    private void handleMissingUploadReferenceId(UploadDefinition upload) {
        Optional<String> refIdOpt = upload.getReferenceId();
        if (!refIdOpt.isPresent()) {
            upload.setReferenceId(Optional.of(DefaultFallback.FALLBACK_UPLOAD_REF_ID.getValue()));
        }
    }

    private void handleWebScan(TestDefinition test, SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        Optional<SecHubWebScanConfiguration> webScanOpt = runSecHubJob.getWebScan();
        if (webScanOpt.isEmpty()) {
            return;
        }
        SecHubWebScanConfiguration webScan = webScanOpt.get();
        LOG.warn("Web scan found, but no special preparation done for url: {}", webScan.getUrl());
        SecHubWebScanApiConfiguration secHubWebScanApiConfiguration = webScan.getApi();
        if (secHubWebScanApiConfiguration == null) {
            return;
        }
        handleUsedDataConfigurationObjects(secHubWebScanApiConfiguration.getUse());
    }

    private void handleInfraScan(TestDefinition test, SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        Optional<SecHubInfrastructureScanConfiguration> infraScanOpt = runSecHubJob.getInfraScan();
        if (infraScanOpt.isEmpty()) {
            return;
        }
        SecHubInfrastructureScanConfiguration infraScan = infraScanOpt.get();
        LOG.warn("Infrastructure scan found, but no special preparation done for uris: {}", infraScan.getUris());

    }

    private void handleCodeScan(TestDefinition test, SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        Optional<SecHubCodeScanConfiguration> codeScanOpt = runSecHubJob.getCodeScan();
        if (codeScanOpt.isEmpty()) {
            return;
        }
        SecHubCodeScanConfiguration codeScan = codeScanOpt.get();
        handleUsedDataConfigurationObjects(codeScan.getUse());
    }

    private void handleLicenseScan(TestDefinition test, SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        Optional<SecHubLicenseScanConfiguration> licenseScanOpt = runSecHubJob.getLicenseScan();
        if (licenseScanOpt.isEmpty()) {
            return;
        }
        SecHubLicenseScanConfiguration licenseScan = licenseScanOpt.get();
        handleUsedDataConfigurationObjects(licenseScan.getUse());
    }

    private void handleSecretScan(TestDefinition test, SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        Optional<SecHubSecretScanConfiguration> secretScanOpt = runSecHubJob.getSecretScan();
        if (secretScanOpt.isEmpty()) {
            return;
        }
        SecHubSecretScanConfiguration secretScan = secretScanOpt.get();
        handleUsedDataConfigurationObjects(secretScan.getUse());
    }

    private void handleUsedDataConfigurationObjects(List<String> usageByNames) {
        if (usageByNames != null && usageByNames.isEmpty()) {
            usageByNames.add(FALLBACK_UPLOAD_REF_ID.getValue());
        }
    }

    /* creates a configuration where variables are replaced with dedicated values */
    private void initializeAlteredConfiguration(SystemTestRuntimeContext context) {
        SystemTestConfiguration newConfiguration = createAlternativeConfigurationWithVariablesReplaced(context);

        context.alterConfguration(newConfiguration);
    }

    private SystemTestConfiguration createAlternativeConfigurationWithVariablesReplaced(SystemTestRuntimeContext context) {
        SystemTestConfiguration originConfiguration = context.getOriginConfiguration();
        String originJson = JSONConverter.get().toJSON(originConfiguration);

        /* --------------------- */
        /* Environment variables */
        /* --------------------- */
        SystemTestTemplateEngine templateEngine = context.getTemplateEngine();
        String modifiedJson = templateEngine.replaceEnvironmentVariablesWithValues(originJson, context.getEnvironmentProvider());
        /*
         * at this point all variable placeholders `${env.XYZ}` have been replaced with
         * the values set in the environment
         */
        if (templateEngine.hasEnvironmentVariables(modifiedJson)) {
            throw new WrongConfigurationException("Cycle detected!\nWe do not allow environment variables which are nesting environment variables again!",
                    context);
        }

        /* -------------- */
        /* User variables */
        /* -------------- */
        int loopCount = 0;
        while (templateEngine.hasUserVariables(modifiedJson)) {
            loopCount++;

            SystemTestConfiguration intermediateConfiguration = JSONConverter.get().fromJSON(SystemTestConfiguration.class, modifiedJson);
            Map<String, String> variables = intermediateConfiguration.getVariables();

            if (loopCount > MAX_VARIABLE_REPLACEMENT_LOOP_COUNT) {
                throw new WrongConfigurationException("Cycle detected!\nA Lopp inside variable replacement has occurred.\nVariables currently:" + variables,
                        context);
            }

            modifiedJson = templateEngine.replaceUserVariablesWithValues(modifiedJson, variables);
        }

        /* ----------------- */
        /* Runtime variables */
        /* ----------------- */
        Map<String, String> runtimeVariables = createRuntimeVariables(context);
        modifiedJson = templateEngine.replaceRuntimeVariablesWithValues(modifiedJson, runtimeVariables);

        SystemTestConfiguration newConfiguration = JSONConverter.get().fromJSON(SystemTestConfiguration.class, modifiedJson);
        return newConfiguration;
    }

    private Map<String, String> createRuntimeVariables(SystemTestRuntimeContext context) {

        Map<String, String> runtimeVariables = new LinkedHashMap<>();
        /*
         * at this point, we are NOT inside a test, we only know this is a calculated
         * variable which is available when the test starts. Because of this, we just
         * rename it to "calculated"
         */
        runtimeVariables.put(RuntimeVariable.CURRENT_TEST_FOLDER.getVariableName(), CalculatedVariables.TEST_WORKING_DIRECTORY.asExpression());
        runtimeVariables.put(RuntimeVariable.ADDITIONAL_RESOURCES_FOLDER.getVariableName(), context.getAdditionalResourcesRoot().toString());
        return runtimeVariables;
    }

    private void prepareLocal(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            LOG.debug("Skip local preparation - run is not local");
            return;
        }
        createDefaultsWhereNothingDefined(context);

        prepareScripts(context);
        collectLocalMetaData(context);

        createFallbackSecHubSetupPartsWithMetaData(context);

        addFallbackExecutorConfigurationCredentials(context);
        addExecutorDefaultsForMissingParts(context);

    }

    private void prepareRemote(SystemTestRuntimeContext context) {
        if (context.isLocalRun()) {
            LOG.debug("Skip remote preparation - run is not remote");
            return;
        }
        /* currently no special remote preparation at all */
    }

    private void addFallbackDefaultProfileToExecutorsWithoutProfile(SystemTestRuntimeContext context) {
        SecHubConfigurationDefinition sechubConfig = context.getLocalSecHubConfigurationOrFail();
        List<SecHubExecutorConfigDefinition> executors = sechubConfig.getExecutors();
        for (SecHubExecutorConfigDefinition executor : executors) {
            Set<String> profiles = executor.getProfiles();
            if (profiles.isEmpty()) {
                profiles.add(FALLBACK_PROFILE_ID.getValue());
            }
        }
    }

    private void addFallbackDefaultProjectAndProfilesWhenNotDefined(SystemTestRuntimeContext context) {
        SecHubConfigurationDefinition sechubConfig = context.getLocalSecHubConfigurationOrFail();
        Optional<List<ProjectDefinition>> projectsOpt = sechubConfig.getProjects();
        if (!projectsOpt.isPresent()) {
            sechubConfig.setProjects(Optional.of(new ArrayList<>()));
        }

        List<ProjectDefinition> projects = sechubConfig.getProjects().get();

        /* handle missing project definitions */
        if (projects.isEmpty()) {
            ProjectDefinition fallbackProject = new ProjectDefinition();

            fallbackProject.setName(FALLBACK_PROJECT_NAME.getValue());
            fallbackProject.setComment("Auto created fallback default project");

            projects.add(fallbackProject);
        }

        /* handle missing profile definitions for projects */
        for (ProjectDefinition projectDefinition : projects) {
            List<String> profiles = projectDefinition.getProfiles();
            if (profiles.isEmpty()) {
                profiles.add(FALLBACK_PROFILE_ID.getValue());
            }
        }
    }

    private void createDefaultsWhereNothingDefined(SystemTestRuntimeContext context) {

        createFallbackSecHubSetupParts(context);
        addFallbackDefaultProjectAndProfilesWhenNotDefined(context);
        createFallbackNamesForExecutorsWithoutName(context);
        addFallbackDefaultProfileToExecutorsWithoutProfile(context);

        createFallbacksForPDSSolutions(context);

    }

    private void createFallbackNamesForExecutorsWithoutName(SystemTestRuntimeContext context) {
        SecHubConfigurationDefinition sechubConfig = context.getLocalSecHubConfigurationOrFail();
        List<SecHubExecutorConfigDefinition> executors = sechubConfig.getExecutors();
        for (SecHubExecutorConfigDefinition executor : executors) {
            String name = executor.getName();
            if (name == null || name.isEmpty()) {
                String productId = executor.getPdsProductId();
                String newName = "systemtest-" + productId;
                if (newName.length() > 30) {
                    newName = newName.substring(0, 30);
                }
                executor.setName(newName);
            }
        }
    }

    private void createFallbacksForPDSSolutions(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            return;
        }
        for (PDSSolutionDefinition localPdsSolution : context.getLocalPdsSolutionsOrFail()) {
            String url = localPdsSolution.getUrl();
            if (url == null) {
                localPdsSolution.setUrl(DefaultFallbackUtil.convertToURL(DefaultFallback.FALLBACK_LOCAL_PDS_URL).toExternalForm());
            }
            if (localPdsSolution.getWaitForAvailable().isEmpty()) {
                localPdsSolution.setWaitForAvailable(Optional.of(DefaultFallbackUtil.convertToBoolean(DefaultFallback.FALLBACK_LOCAL_PDS_WAIT_FOR_AVAILABLE)));
            }
            CredentialsDefinition localPDSSolutionTechUser = localPdsSolution.getTechUser();
            if (localPDSSolutionTechUser.getUserId() == null || localPDSSolutionTechUser.getUserId().isEmpty()) {

                localPDSSolutionTechUser.setUserId(DefaultFallback.FALLBACK_PDS_TECH_USER.getValue());
                localPDSSolutionTechUser.setApiToken(DefaultFallback.FALLBACK_PDS_TECH_TOKEN.getValue());

                LOG.debug("No tech user credentials set for solution: '{}', added defaults");
            }

            CredentialsDefinition localPDSSolutionAdminUser = localPdsSolution.getAdmin();
            if (localPDSSolutionAdminUser.getUserId() == null || localPDSSolutionAdminUser.getUserId().isEmpty()) {

                localPDSSolutionAdminUser.setUserId(DefaultFallback.FALLBACK_PDS_ADMIN_USER.getValue());
                localPDSSolutionAdminUser.setApiToken(DefaultFallback.FALLBACK_PDS_ADMIN_TOKEN.getValue());

                LOG.debug("No admin credentials set for solution: '{}', added defaults");
            }
        }
    }

    private void addFallbackExecutorConfigurationCredentials(SystemTestRuntimeContext context) {
        if (isSecHubConfigurationUnnecessary(context)) {
            return;
        }
        for (SecHubExecutorConfigDefinition localExecutorDefinition : context.getLocalSecHubExecutorConfigurationsOrFail()) {

            String productId = localExecutorDefinition.getPdsProductId();

            PDSSolutionDefinition solutionToExecute = context.fetchPDSSolutionByProductIdOrFail(productId);

            if (localExecutorDefinition.getCredentials().isEmpty()) {
                /* when not defined, define an empty one as fallback */
                localExecutorDefinition.setCredentials(Optional.of(new CredentialsDefinition()));
            }

            CredentialsDefinition executorCredentials = localExecutorDefinition.getCredentials().get();

            if (executorCredentials.getUserId() == null || executorCredentials.getUserId().isEmpty()) {

                /* nothing special defined, so just use the executor credentials! */
                CredentialsDefinition techUser = solutionToExecute.getTechUser();

                String userId = techUser.getUserId();
                if (userId == null || userId.isEmpty()) {
                    throw new IllegalStateException("At this point of the preparation, the tech user credentials must be not null!");
                }

                executorCredentials.setUserId(userId);
                executorCredentials.setApiToken(techUser.getApiToken());

                LOG.debug("No credentials set for executor, reused credentials from pds product");
            }
        }

    }

    private boolean isSecHubConfigurationUnnecessary(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            return true;
        }
        if (!context.isLocalSecHubConfigured()) {
            return true;
        }
        /* it is necessary to configure SecHub parts by the framework */
        return false;
    }

    private void addExecutorDefaultsForMissingParts(SystemTestRuntimeContext context) {
        if (isSecHubConfigurationUnnecessary(context)) {
            return;
        }

        for (SecHubExecutorConfigDefinition definition : context.getLocalSecHubExecutorConfigurationsOrFail()) {

            Map<String, String> params = definition.getParameters();
            if (params.get("sechub.productexecutor.pds.trustall.certificates") == null) {
                params.put("sechub.productexecutor.pds.trustall.certificates", "true");
                LOG.debug("No trust all definition defined for product executor, defined default");
            }

            if (params.get("sechub.productexecutor.pds.timetowait.nextcheck.milliseconds") == null) {
                params.put("sechub.productexecutor.pds.timetowait.nextcheck.milliseconds", "500");
                LOG.debug("No next check time found for PDS product executor, defined default");
            }
            if (params.get(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE) == null) {
                params.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE, "true");
                LOG.debug("No SecHub storage usage definition found, defined default");
            }
            if (params.get(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_DEBUG_ENABLED) == null) {
                params.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_DEBUG_ENABLED, "true");
                LOG.debug("No SecHub PDS debug property definition found, defined default");
            }
            if (definition.getVersion() == 0) {
                definition.setVersion(1);
                LOG.debug("No executor version found, defined default");
            }
        }

    }

    private void createFallbackSecHubSetupParts(SystemTestRuntimeContext context) {
        if (isSecHubConfigurationUnnecessary(context)) {
            return;
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        LocalSecHubDefinition secHub = localSetup.getSecHub();

        if (secHub.getUrl() == null) {
            String defaultValue = DefaultFallback.FALLBACK_LOCAL_SECHUB_URL.getValue();
            secHub.setUrl(defaultValue);
            LOG.info("No URL set for local SecHub, added default url:" + secHub.getUrl());
        }
        CredentialsDefinition admin = secHub.getAdmin();

        if (admin.getUserId() == null || admin.getUserId().isEmpty()) {
            admin.setUserId(DefaultFallback.FALLBACK_SECHUB_ADMIN_USER.getValue());
            admin.setApiToken(DefaultFallback.FALLBACK_SECHUB_ADMIN_TOKEN.getValue());
        }

        if (secHub.getWaitForAvailable().isEmpty()) {
            secHub.setWaitForAvailable(Optional.of(DefaultFallbackUtil.convertToBoolean(DefaultFallback.FALLBACK_SECHUB_WAIT_FOR_AVAILABLE)));
        }
    }

    private void createFallbackSecHubSetupPartsWithMetaData(SystemTestRuntimeContext context) {
        if (isSecHubConfigurationUnnecessary(context)) {
            return;
        }
        for (SecHubExecutorConfigDefinition executorConfigDefinition : context.getLocalSecHubConfigurationOrFail().getExecutors()) {
            String definedBaseUrl = executorConfigDefinition.getBaseURL();
            if (definedBaseUrl == null) {
                String pdsProductId = executorConfigDefinition.getPdsProductId();
                executorConfigDefinition.setBaseURL(calculateMissingBaseUrlStringForPdsProduct(pdsProductId, context));
            }
        }

    }

    private String calculateMissingBaseUrlStringForPdsProduct(String pdsProductId, SystemTestRuntimeContext context) {
        PDSSolutionDefinition solution = context.getPDSSolutionDefinitionOrFail(pdsProductId);
        return solution.getUrl();
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
                Path pdsServerConfigFilePath = pdsWorkingDirectory.resolve(SUB_PATH_PDS_CONFIG_JSON);
                solution.setPathToPdsServerConfigFile(pdsServerConfigFilePath.toString());
            }

            prepareScriptData(solution.getStart(), pdsWorkingDirectory);
            prepareScriptData(solution.getStop(), pdsWorkingDirectory);
        }
    }

    private void prepareScriptData(List<ExecutionStepDefinition> steps, Path workingDirectory) {
        for (ExecutionStepDefinition step : steps) {
            if (step.getScript().isPresent()) {
                ScriptDefinition scriptDefinition = step.getScript().get();
                scriptDefinition.setWorkingDir(workingDirectory.toString());
            }
        }
    }

    private Path resolvePdsSolutionWorkingDirectory(PDSSolutionDefinition solution, SystemTestRuntimeContext context) {
        Path pdsSolutionsRootFolder = context.getLocationSupport().getPDSSolutionRoot();
        if (pdsSolutionsRootFolder == null) {
            throw new WrongConfigurationException("PDS solutions root folder is not defined - but must!", context);
        }
        String solutionName = solution.getName();
        if (solutionName == null) {
            throw new WrongConfigurationException("At least one solution name is not set", context);
        }
        String pdsBaseDirAsString = solution.getBaseDir();
        if (pdsBaseDirAsString == null) {
            pdsBaseDirAsString = pdsSolutionsRootFolder.resolve(solutionName).toAbsolutePath().toString();
            LOG.debug("Base dir not set for PDS solution: {}, so calculate base dir by name and root dir to: {}", solutionName, pdsBaseDirAsString);
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
