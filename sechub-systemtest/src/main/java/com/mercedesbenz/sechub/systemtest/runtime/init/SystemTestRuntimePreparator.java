package com.mercedesbenz.sechub.systemtest.runtime.init;

import static com.mercedesbenz.sechub.systemtest.config.DefaultFallback.*;

import java.net.MalformedURLException;
import java.net.URL;
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

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationUsageByName;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
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
        handleUsedDataConfigurationObjects(codeScan, test, context);
    }

    private void handleLicenseScan(TestDefinition test, SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        Optional<SecHubLicenseScanConfiguration> licenseScanOpt = runSecHubJob.getLicenseScan();
        if (licenseScanOpt.isEmpty()) {
            return;
        }
        SecHubLicenseScanConfiguration licenseScan = licenseScanOpt.get();
        handleUsedDataConfigurationObjects(licenseScan, test, context);
    }

    private void handleSecretScan(TestDefinition test, SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        Optional<SecHubSecretScanConfiguration> secretScanOpt = runSecHubJob.getSecretScan();
        if (secretScanOpt.isEmpty()) {
            return;
        }
        SecHubSecretScanConfiguration secretScan = secretScanOpt.get();
        handleUsedDataConfigurationObjects(secretScan, test, context);
    }

    private void handleUsedDataConfigurationObjects(SecHubDataConfigurationUsageByName usageByName, TestDefinition test, SystemTestRuntimeContext context) {
        if (usageByName.getNamesOfUsedDataConfigurationObjects().isEmpty()) {
            usageByName.getNamesOfUsedDataConfigurationObjects().add(FALLBACK_UPLOAD_REF_ID.getValue());
        }
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
        runtimeVariables.put(RuntimeVariable.WORKSPACE_ROOT.getVariableName(), context.getWorkspaceRoot().toString());
        runtimeVariables.put(RuntimeVariable.PDS_SOLUTIONS_ROOT.getVariableName(), context.getLocationSupport().getPDSSolutionRoot().toString());

        runtimeVariables.put(RuntimeVariable.CURRENT_TEST_FOLDER.getVariableName(), CalculatedVariables.CURRENT_TEST_FOLDER.asExpression());
        return runtimeVariables;
    }

    private void prepareLocal(SystemTestRuntimeContext context) {
        createDefaultsWhereNothingDefined(context);

        prepareScripts(context);
        collectLocalMetaData(context);

        createFallbackSecHubSetupPartsWithMetaData(context);

        addFallbackExecutorConfigurationCredentials(context);

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

    private void createFallbackDefaultProjectWhenNoProjectsDefined(SystemTestRuntimeContext context) {
        SecHubConfigurationDefinition sechubConfig = context.getLocalSecHubConfigurationOrFail();
        Optional<List<ProjectDefinition>> projects = sechubConfig.getProjects();
        if (!projects.isPresent()) {
            sechubConfig.setProjects(Optional.of(new ArrayList<>()));
        }

        List<ProjectDefinition> projectDefinitions = sechubConfig.getProjects().get();
        if (projectDefinitions.isEmpty()) {

            ProjectDefinition fallback = new ProjectDefinition();

            fallback.setName(FALLBACK_PROJECT_NAME.getValue());
            fallback.setComment("Auto created fallback default project");
            fallback.getProfiles().add(FALLBACK_PROFILE_ID.getValue());
            projectDefinitions.add(fallback);
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

        createFallbackSecHubSetupParts(context);
        createFallbackDefaultProjectWhenNoProjectsDefined(context);
        addFallbackDefaultProfileToExecutorsWithoutProfile(context);

        createFallbackForPDSSolutions(context);

    }

    private void createFallbackForPDSSolutions(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            return;
        }
        for (PDSSolutionDefinition localPdsSolution : context.getLocalPdsSolutionsOrFail()) {
            URL url = localPdsSolution.getUrl();
            if (url == null) {
                localPdsSolution.setUrl(DefaultFallbackUtil.convertToURL(DefaultFallback.FALLBACK_LOCAL_PDS_URL));
            }
            if (localPdsSolution.getWaitForAvailable().isEmpty()) {
                localPdsSolution.setWaitForAvailable(Optional.of(DefaultFallbackUtil.convertToBoolean(DefaultFallback.FALLBACK_LOCAL_PDS_WAIT_FOR_AVAILABLE)));
            }
            CredentialsDefinition credentials = localPdsSolution.getTechUser();
            if (credentials.getUserId() == null || credentials.getUserId().isEmpty()) {

                credentials.setUserId(DefaultFallback.FALLBACK_PDS_TECH_USER.getValue());
                credentials.setApiToken(DefaultFallback.FALLBACK_PDS_TECH_TOKEN.getValue());

                LOG.info("No credentials set for solution: '{}', added defaults");
            }
        }
    }

    private void addFallbackExecutorConfigurationCredentials(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            return;
        }
        if (!context.isLocalSecHubConfigured()) {
            return;
        }
        for (SecHubExecutorConfigDefinition definition : context.getLocalSecHubExecutorConfigurationsOrFail()) {
            CredentialsDefinition credentials = definition.getCredentials();
            String productId = definition.getPdsProductId();

            PDSSolutionDefinition solution = context.fetchPDSSolutionByProductIdOrFail(productId);

            if (credentials.getUserId() == null || credentials.getUserId().isEmpty()) {

                CredentialsDefinition techUser = solution.getTechUser();

                String userId = techUser.getUserId();
                if (userId == null || userId.isEmpty()) {
                    throw new IllegalStateException("At this point of preparation, the tech user credentials must be not null!");
                }

                credentials.setUserId(userId);
                credentials.setApiToken(techUser.getApiToken());

                LOG.info("No credentials set for executor, added defaults");
            }
        }

    }

    private void createFallbackSecHubSetupParts(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            return;
        }
        if (!context.isLocalSecHubConfigured()) {
            return;
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        LocalSecHubDefinition secHub = localSetup.getSecHub();

        if (secHub.getUrl() == null) {
            String defaultValue = DefaultFallback.FALLBACK_LOCAL_SECHUB_URL.getValue();
            try {
                secHub.setUrl(new URL(defaultValue));
                LOG.info("No URL set for local SecHub, added default url:" + secHub.getUrl());

            } catch (MalformedURLException e) {
                throw new IllegalStateException("Default value is not an URL:" + defaultValue);
            }
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
        if (!context.isLocalRun()) {
            return;
        }
        if (!context.isLocalSecHubConfigured()) {
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
        URL definedurl = solution.getUrl();
        if (definedurl != null) {
            return definedurl.toString();
        }
        return null;
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