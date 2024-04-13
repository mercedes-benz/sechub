// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.init;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult.SecHubConfigurationModelValidationErrorData;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;
import com.mercedesbenz.sechub.systemtest.config.CredentialsDefinition;
import com.mercedesbenz.sechub.systemtest.config.ExecutionStepDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.RemoteSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.RemoteSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinitionTransformer;
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubExecutorConfigDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.UploadDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionScope;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestExecutionState;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeMetaData;
import com.mercedesbenz.sechub.systemtest.runtime.WrongConfigurationException;

public class SystemTestRuntimeContextHealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeContextHealthCheck.class);

    public void check(SystemTestRuntimeContext context) {

        checkLocal(context);
        checkRemote(context);
        checkTests(context);
    }

    private void checkTests(SystemTestRuntimeContext context) {
        List<TestDefinition> tests = context.getConfiguration().getTests();
        for (TestDefinition test : tests) {
            if (SimpleStringUtils.isEmpty(test.getName())) {
                throw new WrongConfigurationException("Found at least one test without a name! Every test must have its own name!", context);
            }
            assertPreparationScriptPathsAreSet(context, test);
            assertRunSecHubJobCorrect(context, test);
        }
    }

    private void assertRunSecHubJobCorrect(SystemTestRuntimeContext context, TestDefinition test) {
        Optional<RunSecHubJobDefinition> runSecHubJobOpt = test.getExecute().getRunSecHubJob();
        if (runSecHubJobOpt.isEmpty()) {
            return;
        }
        RunSecHubJobDefinition runSecHubJob = runSecHubJobOpt.get();
        assertReferenceIds(context, runSecHubJob);

        /* last but not least */
        RunSecHubJobDefinitionTransformer transformer = new RunSecHubJobDefinitionTransformer();
        SecHubConfigurationModel model = transformer.transformToSecHubConfiguration(runSecHubJob);
        SecHubConfigurationModelValidator validator = new SecHubConfigurationModelValidator();
        SecHubConfigurationModelValidationResult result = validator.validate(model);
        if (result.hasErrors()) {
            for (SecHubConfigurationModelValidationErrorData errorData : result.getErrors()) {
                throw new WrongConfigurationException("Test: " + test.getName() + " leads to an invalid sechub configurarion:" + errorData.toString(), context);
            }
        }

    }

    private void assertReferenceIds(SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        Set<String> existingReferenceIds = collectReferenceIdsAndFailIfMissing(context, runSecHubJob);

        assertReferencesCorrectForCodeScan(context, runSecHubJob, existingReferenceIds);
        assertReferencesCorrectForWebScan(context, runSecHubJob, existingReferenceIds);
        assertReferencesCorrectForLicenseScan(context, runSecHubJob, existingReferenceIds);
        assertReferencesCorrectForSecretcan(context, runSecHubJob, existingReferenceIds);

        /* Because infrastructure scans do not have references we do not handled them */
    }

    private void assertReferencesCorrectForCodeScan(SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob, Set<String> existingReferenceIds) {
        Optional<SecHubCodeScanConfiguration> codeConfigOpt = runSecHubJob.getCodeScan();
        if (codeConfigOpt.isPresent()) {
            assertReferenceIdsCorrect("code scan", context, existingReferenceIds, codeConfigOpt.get().getNamesOfUsedDataConfigurationObjects());
        }
    }

    private void assertReferencesCorrectForSecretcan(SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob, Set<String> existingReferenceIds) {
        Optional<SecHubSecretScanConfiguration> secretConfigOpt = runSecHubJob.getSecretScan();
        if (secretConfigOpt.isPresent()) {
            assertReferenceIdsCorrect("secret scan", context, existingReferenceIds, secretConfigOpt.get().getNamesOfUsedDataConfigurationObjects());
        }
    }

    private void assertReferencesCorrectForLicenseScan(SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob,
            Set<String> existingReferenceIds) {
        Optional<SecHubLicenseScanConfiguration> licenseConfigOpt = runSecHubJob.getLicenseScan();
        if (licenseConfigOpt.isPresent()) {
            assertReferenceIdsCorrect("license scan", context, existingReferenceIds, licenseConfigOpt.get().getNamesOfUsedDataConfigurationObjects());
        }
    }

    private void assertReferencesCorrectForWebScan(SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob, Set<String> existingReferenceIds) {
        Optional<SecHubWebScanConfiguration> webConfigOpt = runSecHubJob.getWebScan();
        if (webConfigOpt.isPresent()) {
            Optional<SecHubWebScanApiConfiguration> apiOpt = webConfigOpt.get().getApi();
            if (apiOpt.isEmpty()) {
                return;
            }
            SecHubWebScanApiConfiguration api = apiOpt.get();
            assertReferenceIdsCorrect("web scan api", context, existingReferenceIds, api.getNamesOfUsedDataConfigurationObjects());
        }
    }

    private Set<String> collectReferenceIdsAndFailIfMissing(SystemTestRuntimeContext context, RunSecHubJobDefinition runSecHubJob) {
        List<UploadDefinition> uploads = runSecHubJob.getUploads();
        Set<String> existingReferenceIds = new TreeSet<>();
        for (UploadDefinition upload : uploads) {
            Optional<String> refIdOpt = upload.getReferenceId();
            if (refIdOpt.isEmpty()) {
                throw new WrongConfigurationException("There is at least one upload defined without a reference-id", context);
            }
            String referenceId = refIdOpt.get();
            if (existingReferenceIds.contains(referenceId)) {
                throw new WrongConfigurationException("The reference id:" + referenceId + " is used in multiple uploads. This is currently not supported1",
                        context);
            }
            existingReferenceIds.add(referenceId);
        }
        return existingReferenceIds;
    }

    private void assertReferenceIdsCorrect(String configName, SystemTestRuntimeContext context, Set<String> existingReferenceIds,
            Set<String> referencedByScanOrNull) {
        if (referencedByScanOrNull == null) {
            return;
        }
        for (String name : referencedByScanOrNull) {
            if (!existingReferenceIds.contains(name)) {
                throw new WrongConfigurationException("The reference id:" + name + " is used in  " + configName
                        + " configuration, but there is no upload which references it.\nFound reference ids:" + referencedByScanOrNull, context);
            }
        }
    }

    private void assertPreparationScriptPathsAreSet(SystemTestRuntimeContext context, TestDefinition test) {
        for (ExecutionStepDefinition step : test.getPrepare()) {
            if (step.getScript().isPresent()) {
                ScriptDefinition script = step.getScript().get();
                String path = script.getPath();
                if (SimpleStringUtils.isEmpty(path)) {
                    throw new WrongConfigurationException("Path is missing for script in prepare step for test: " + test.getName(), context);
                }
            }
        }
    }

    private void checkRemote(SystemTestRuntimeContext context) {
        if (context.isLocalRun()) {
            return;
        }
        verifySecHubRemote(context);
    }

    private void verifySecHubRemote(SystemTestRuntimeContext context) {
        RemoteSetupDefinition remoteSetup = context.getRemoteSetupOrFail();
        RemoteSecHubDefinition secHub = remoteSetup.getSecHub();

        CredentialsDefinition user = secHub.getUser();

        String userId = user.getUserId();
        if (userId == null || userId.isEmpty()) {
            throw new WrongConfigurationException("SecHub remote user name not configured, but required for remote run!", context);
        }

        String apiToken = user.getApiToken();
        if (apiToken == null || apiToken.isEmpty()) {
            throw new WrongConfigurationException("SecHub remote user api token not configured, but required for remote run!", context);
        }
    }

    private void checkLocal(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            LOG.debug("Skip local health check parts - run is not local");
            return;
        }

        verifySecHubLocal(context);
        verifyProductIdentifiersForLocalPDS(context);
        verifyExecutorConfigurations(context);
    }

    private void verifySecHubLocal(SystemTestRuntimeContext context) {
        if (!context.isLocalSecHubConfigured()) {
            return;
        }
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        LocalSecHubDefinition secHub = localSetup.getSecHub();

        CredentialsDefinition user = secHub.getAdmin();

        if (secHub.getUrl() == null) {
            throw new WrongConfigurationException("SecHub local url not configured but necessary for local run!", context);
        }

        String userId = user.getUserId();
        if (userId == null || userId.isEmpty()) {
            throw new WrongConfigurationException("SecHub local admin user name not configured but necessary for local run!", context);
        }

        String apiToken = user.getApiToken();
        if (apiToken == null || apiToken.isEmpty()) {
            throw new WrongConfigurationException("SecHub local admin user name not configured but necessary for local run!", context);
        }

        assertSteps(secHub.getStart(), SystemTestExecutionScope.SECHUB.name(), SystemTestExecutionState.START, context);
        assertSteps(secHub.getStop(), SystemTestExecutionScope.SECHUB.name(), SystemTestExecutionState.STOP, context);

        for (PDSSolutionDefinition pdsSolution : localSetup.getPdsSolutions()) {
            assertSteps(pdsSolution.getStart(), SystemTestExecutionScope.PDS_SOLUTION.name() + ":" + pdsSolution.getName(), SystemTestExecutionState.START,
                    context);
            assertSteps(pdsSolution.getStop(), SystemTestExecutionScope.PDS_SOLUTION.name() + ":" + pdsSolution.getName(), SystemTestExecutionState.STOP,
                    context);
        }

    }

    private void assertSteps(List<ExecutionStepDefinition> startSteps, String scope, SystemTestExecutionState state, SystemTestRuntimeContext context) {
        if (startSteps.isEmpty()) {
            return;
        }

        for (ExecutionStepDefinition step : startSteps) {
            Optional<ScriptDefinition> scriptOpt = step.getScript();
            if (scriptOpt.isPresent()) {
                ScriptDefinition script = scriptOpt.get();
                String workingDirectoryAsString = script.getWorkingDirectory();
                if (workingDirectoryAsString == null) {
                    throw new WrongConfigurationException(scope + " " + state + ": script without working directory", context);
                }
                assertScriptPathExists(scope, state, "script working directory", context, workingDirectoryAsString);

                String pathAsString = script.getPath();
                if (pathAsString == null) {
                    throw new WrongConfigurationException(scope + " " + state + ": script without path", context);
                }
                String fullScriptPath = Paths.get(pathAsString).resolve(workingDirectoryAsString).toAbsolutePath().toString();
                assertScriptPathExists(scope, state, "full script path", context, fullScriptPath);

            }
        }
    }

    private void assertScriptPathExists(String scope, SystemTestExecutionState state, String info, SystemTestRuntimeContext context, String pathAsString) {
        try {
            Path path = Paths.get(pathAsString);
            if (!Files.exists(path)) {
                throw new WrongConfigurationException(scope + " " + state + ": " + info + " - non existing path:" + path, context);
            }
        } catch (InvalidPathException e) {
            throw new WrongConfigurationException(scope + " " + state + ": " + info + " - invalid path:" + pathAsString, context, e);
        }
    }

    private void verifyExecutorConfigurations(SystemTestRuntimeContext context) {

        SecHubConfigurationDefinition sechubConfig = context.getLocalSecHubConfigurationOrFail();
        for (SecHubExecutorConfigDefinition executorConfigDefinition : sechubConfig.getExecutors()) {
            assertExecutorConfigNameDefined(context, executorConfigDefinition);
            assertExecutorConfigBaseUrlDefined(context, executorConfigDefinition);
            assertExecutorParametersValid(context, executorConfigDefinition);
        }
    }

    private void assertExecutorConfigNameDefined(SystemTestRuntimeContext context, SecHubExecutorConfigDefinition executorConfigDefinition) {
        String name = executorConfigDefinition.getName();
        if (name == null || name.isEmpty()) {
            String productId = executorConfigDefinition.getPdsProductId();
            throw new WrongConfigurationException("For product id:" + productId + " an executor definition has no name!", context);
        }

    }

    private void assertExecutorParametersValid(SystemTestRuntimeContext context, SecHubExecutorConfigDefinition executorConfigDefinition) {

        String productId = executorConfigDefinition.getPdsProductId();
        PDSProductSetup productSetup = assertProductSetupAvailable(context, productId);
        PDSProductParameterSetup pdsParamSetup = productSetup.getParameters();

        assertMandatoryParametersAreDefined(context, executorConfigDefinition, productId, pdsParamSetup);
    }

    private PDSProductSetup assertProductSetupAvailable(SystemTestRuntimeContext context, String productId) {
        PDSProductSetup productSetup = context.getPDSProductSetupOrNull(productId);
        if (productSetup == null) {
            throw new WrongConfigurationException("For product id:" + productId + " no product setup available!", context);
        }
        return productSetup;
    }

    private void assertMandatoryParametersAreDefined(SystemTestRuntimeContext context, SecHubExecutorConfigDefinition executorConfigDefinition,
            String productId, PDSProductParameterSetup pdsParamSetup) {
        Map<String, String> parameters = executorConfigDefinition.getParameters();
        List<PDSProductParameterDefinition> mandatoryParams = pdsParamSetup.getMandatory();
        for (PDSProductParameterDefinition mandatoryParam : mandatoryParams) {
            if (mandatoryParam.hasDefault()) {
                /* with default it doesn't matter */
                continue;
            }
            String mandatoryKey = mandatoryParam.getKey();
            if (!parameters.containsKey(mandatoryKey)) {
                throw new WrongConfigurationException(
                        "The excutor config definition for pds product:" + productId + " has not defined mandatory key:" + mandatoryKey, context);
            }
        }
    }

    private void assertExecutorConfigBaseUrlDefined(SystemTestRuntimeContext context, SecHubExecutorConfigDefinition executorConfigDefinition) {
        String baseUrl = executorConfigDefinition.getBaseURL();
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new WrongConfigurationException(
                    "Base url not defined for executor configuration for pds product :" + executorConfigDefinition.getPdsProductId(), context);
        }
    }

    private void verifyProductIdentifiersForLocalPDS(SystemTestRuntimeContext context) {

        SecHubConfigurationDefinition sechubConfig = context.getLocalSecHubConfigurationOrFail();
        SystemTestRuntimeMetaData runtimeMetaData = context.getRuntimeMetaData();

        Set<String> allProductIdentifiers = new LinkedHashSet<>();

        /*
         * verify the server configurations are not having a duplicated product
         * identifier
         */
        Collection<PDSServerConfiguration> serverConfigurations = runtimeMetaData.getPDSServerConfigurations();
        for (PDSServerConfiguration serverConfig : serverConfigurations) {
            List<PDSProductSetup> productSetups = serverConfig.getProducts();
            for (PDSProductSetup productSetup : productSetups) {
                String foundProductId = productSetup.getId();
                if (allProductIdentifiers.contains(foundProductId)) {
                    throw new IllegalStateException("This is odd: There are two PDS product setups which both define a product with ID:" + foundProductId
                            + "! This may never happen!\nPlease change your PDS server configuration file and provide another product ID!");
                }
                allProductIdentifiers.add(foundProductId);
            }
        }

        /*
         * verify the defined SecHub configuration parts are using the available product
         * identifiers
         */
        for (SecHubExecutorConfigDefinition executorDefinition : sechubConfig.getExecutors()) {
            String productId = executorDefinition.getPdsProductId();
            if (!allProductIdentifiers.contains(productId)) {
                throw new WrongConfigurationException("Cannot resolve PDS product.\n" + productId + " not found"
                        + "\nFound product identifiers inside PDS server configuration(s):\n" + allProductIdentifiers, context);
            }
        }
    }
}
