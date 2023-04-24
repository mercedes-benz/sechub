package com.mercedesbenz.sechub.systemtest.runtime;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.mercedesbenz.sechub.systemtest.config.ScriptDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubExecutorConfigDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestExecutionScope;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestExecutionState;

public class SystemTestRuntimeHealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeHealthCheck.class);

    public void check(SystemTestRuntimeContext context) {

        checkLocal(context);
        checkRemote(context);
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
            throw new WrongConfigurationException("SecHub remote user name not configured but necessary for remote run!", context);
        }

        String apiToken = user.getApiToken();
        if (apiToken == null || apiToken.isEmpty()) {
            throw new WrongConfigurationException("SecHub remote user api token not configured but necessary for remote run!", context);
        }
    }

    private void checkLocal(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            LOG.debug("Skip local health check parts - run is not local");
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

        asssertSteps(secHub.getStart(), SystemTestExecutionScope.SECHUB.name(), SystemTestExecutionState.START, context);
        asssertSteps(secHub.getStop(), SystemTestExecutionScope.SECHUB.name(), SystemTestExecutionState.STOP, context);

        for (PDSSolutionDefinition pdsSolution : localSetup.getPdsSolutions()) {
            asssertSteps(pdsSolution.getStart(), SystemTestExecutionScope.PDS_SOLUTION.name() + ":" + pdsSolution.getName(), SystemTestExecutionState.START,
                    context);
            asssertSteps(pdsSolution.getStop(), SystemTestExecutionScope.PDS_SOLUTION.name() + ":" + pdsSolution.getName(), SystemTestExecutionState.STOP,
                    context);
        }

    }

    private void asssertSteps(List<ExecutionStepDefinition> startSteps, String scope, SystemTestExecutionState state, SystemTestRuntimeContext context) {
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
            assertExecutorConfigBaseUrlDefined(context, executorConfigDefinition);
            assertExecutorParametersValid(context, executorConfigDefinition);
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
                    throw new IllegalStateException("This is odd: There are two PDS product setups which do both define a product with ID:" + foundProductId
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
