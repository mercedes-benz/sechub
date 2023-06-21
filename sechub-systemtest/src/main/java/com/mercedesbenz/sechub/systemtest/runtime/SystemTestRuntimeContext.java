package com.mercedesbenz.sechub.systemtest.runtime;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;
import com.mercedesbenz.sechub.systemtest.config.AbstractSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.CredentialsDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.ProjectDefinition;
import com.mercedesbenz.sechub.systemtest.config.RemoteSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubExecutorConfigDefinition;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.pdsclient.PDSClient;
import com.mercedesbenz.sechub.systemtest.runtime.variable.EnvironmentProvider;

public class SystemTestRuntimeContext {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeContext.class);

    SystemTestConfiguration originConfiguration;
    boolean localRun;
    boolean secHubStarted;
    boolean atLeastOnePDSStarted;

    EnvironmentProvider environmentProvider;
    LocationSupport locationSupport;
    Path workspaceRoot;

    private SystemTestConfiguration configuration;
    private SystemTestRunResult currentResult;
    private Set<SystemTestRunResult> results = new LinkedHashSet<>();
    private SystemTestRuntimeMetaData runtimeMetaData = new SystemTestRuntimeMetaData();
    private Map<PDSSolutionDefinition, PDSSolutionRuntimeData> pdsSolutionRuntimeDataMap = new LinkedHashMap<>();
    private SystemTestRuntimeStage currentStage;
    private List<SystemTestRuntimeStage> stages = new ArrayList<>();
    private SecHubClient remoteUserSecHubClient;
    private SecHubClient localAdminSecHubClient;

    private boolean dryRun;

    private Map<String, PDSClient> localTechUserPdsClientMap = new TreeMap<>();

    public void alterConfguration(SystemTestConfiguration configuration) {
        this.configuration = configuration;
    }

    public EnvironmentProvider getEnvironmentProvider() {
        return environmentProvider;
    }

    public LocationSupport getLocationSupport() {
        return locationSupport;
    }

    public SystemTestRunResult getCurrentResult() {
        return currentResult;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    /* only for tests */
    SystemTestRuntimeContext() {
    }

    public SystemTestRuntimeContext(SystemTestConfiguration originConfiguration, Path workspaceRoot, boolean localRun, boolean dryRun) {
        if (originConfiguration == null) {
            throw new IllegalArgumentException("Origin configuration may never be null!");
        }
        if (workspaceRoot == null) {
            throw new IllegalArgumentException("Workspace root may never be null!");
        }
        this.originConfiguration = originConfiguration;
        this.configuration = originConfiguration;

        this.localRun = localRun;
        this.dryRun = dryRun;

        this.workspaceRoot = workspaceRoot;
    }

    public Path getWorkspaceRoot() {
        return workspaceRoot;
    }

    /**
     * Returns the original configuration without any changes.
     *
     * @return origin configuration (not altered)
     */
    public SystemTestConfiguration getOriginConfiguration() {
        return originConfiguration;
    }

    public boolean isLocalRun() {
        return localRun;
    }

    public void markSecHubStarted() {
        this.secHubStarted = true;
    }

    public boolean isSecHubStarted() {
        return secHubStarted;
    }

    public void markAtLeastOnePDSSolutionStarted() {
        this.atLeastOnePDSStarted = true;
    }

    public boolean isAtLeastOnePDSStarted() {
        return atLeastOnePDSStarted;
    }

    /**
     * Returns the configuration used by runtime. Is enhanced automatically in
     * preparation phase.
     *
     * @return configuration (altered by runtime)
     */
    public SystemTestConfiguration getConfiguration() {
        return configuration;
    }

    public Set<SystemTestRunResult> getResults() {
        return results;
    }

    public void testStarted(TestDefinition test) {
        this.currentResult = new SystemTestRunResult(test.getName());
        results.add(currentResult);
    }

    public Map<PDSSolutionDefinition, PDSSolutionRuntimeData> getPdsSolutionRuntimeDataMap() {
        return pdsSolutionRuntimeDataMap;
    }

    public LocalSetupDefinition getLocalSetupOrFail() {
        Optional<LocalSetupDefinition> localOpt = configuration.getSetup().getLocal();
        if (localOpt.isEmpty()) {
            throw new WrongConfigurationException("To run a local system tests the local setup must be configured!", this);
        }

        return localOpt.get();
    }

    public RemoteSetupDefinition getRemoteSetupOrFail() {
        Optional<RemoteSetupDefinition> localOpt = configuration.getSetup().getRemote();
        if (localOpt.isEmpty()) {
            throw new WrongConfigurationException("To run a remote system tests the remote setup must be configured!", this);
        }

        return localOpt.get();

    }

    public SecHubConfigurationDefinition getLocalSecHubConfigurationOrFail() {
        LocalSecHubDefinition sechub = getLocalSecHubOrFail();
        SecHubConfigurationDefinition sechubConfig = sechub.getConfigure();
        return sechubConfig;
    }

    public LocalSecHubDefinition getLocalSecHubOrFail() {
        LocalSetupDefinition localSetup = getLocalSetupOrFail();
        LocalSecHubDefinition sechub = localSetup.getSecHub();
        return sechub;
    }

    public SystemTestRuntimeMetaData getRuntimeMetaData() {
        return runtimeMetaData;
    }

    public SystemTestRuntimeStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(SystemTestRuntimeStage stage) {
        this.currentStage = stage;
        this.stages.add(stage);
    }

    public List<SystemTestRuntimeStage> getStages() {
        return Collections.unmodifiableList(stages);
    }

    public PDSClient getLocalTechUserPDSClient(String solutionId) {
        if (solutionId == null) {
            throw new IllegalStateException("solution id may not be null!");
        }
        if (!isLocalRun()) {
            throw new IllegalStateException(
            /* @formatter:off */
                      "This is a remote run - which does not need local client..."
                    + "This means the logic inside system test framework has a bug inside!");
            /* @formatter:on */
        }
        final PDSSolutionDefinition pdsSolution = fetchPDSSolutionOrFail(solutionId);
        return localTechUserPdsClientMap.computeIfAbsent(solutionId, (id) -> createPDSClient(pdsSolution));
    }

    public PDSSolutionDefinition fetchPDSSolutionOrFail(String solutionId) {
        List<PDSSolutionDefinition> localPDSSolutions = getLocalPdsSolutionsOrFail();
        PDSSolutionDefinition solutionToUse = null;
        for (PDSSolutionDefinition pdsSolution : localPDSSolutions) {
            String name = pdsSolution.getName();
            if (solutionId.equals(name)) {
                solutionToUse = pdsSolution;
                break;
            }
        }
        if (solutionToUse == null) {
            throw new WrongConfigurationException("There is no solution available with id: " + solutionId, this);
        }
        final PDSSolutionDefinition pdsSolution = solutionToUse;
        return pdsSolution;
    }

    public SecHubClient getLocalAdminSecHubClient() {
        if (!isLocalRun()) {
            throw new IllegalStateException(
            /* @formatter:off */
                      "This is a remote run - which does not need local client..."
                    + "This means the logic inside system test framework has a bug inside!");
            /* @formatter:on */
        }
        if (localAdminSecHubClient == null) {
            LocalSecHubDefinition localSecHub = getLocalSetupOrFail().getSecHub();
            localAdminSecHubClient = createSecHubClient(localSecHub, localSecHub.getAdmin());
        }
        return localAdminSecHubClient;
    }

    public SecHubClient getRemoteUserSecHubClient() {
        if (isLocalRun()) {
            throw new IllegalStateException(
            /* @formatter:off */
                      "This is a local run which does not need remote user client."
                    + "This means the logic inside system test framework has a bug inside!");
            /* @formatter:on */
        }
        if (remoteUserSecHubClient == null) {
            LocalSecHubDefinition localSecHub = getLocalSetupOrFail().getSecHub();
            remoteUserSecHubClient = createSecHubClient(localSecHub, localSecHub.getAdmin());

            LOG.info("Created remote user client for user: {}, apiToken: '{}'", remoteUserSecHubClient.getUsername(),
                    "*".repeat(remoteUserSecHubClient.getSealedApiToken().length()));
        }
        return remoteUserSecHubClient;
    }

    private SecHubClient createSecHubClient(AbstractSecHubDefinition secHubDefinition, CredentialsDefinition credentials) {
        SecHubClient client = null;

        URL url = secHubDefinition.getUrl();
        if (url == null) {
            throw new WrongConfigurationException("URL not defined for local sechub server", this);
        }

        try {
            URI serverUri = url.toURI();
            client = new SecHubClient(serverUri, credentials.getUserId(), credentials.getApiToken(), true);
            LOG.info("Created SecHub client for user: '{}', apiToken: '{}'", client.getUsername(), "*".repeat(client.getSealedApiToken().length()));
        } catch (URISyntaxException e) {
            throw new WrongConfigurationException("URL not defined correct local sechub server: " + e.getMessage(), this);
        }
        return client;
    }

    private PDSClient createPDSClient(PDSSolutionDefinition solutionToUse) {

        PDSClient client = null;

        URL url = solutionToUse.getUrl();
        if (url == null) {
            throw new WrongConfigurationException("URL not defined for PDS solution: " + solutionToUse.getName(), this);
        }

        CredentialsDefinition credentials = solutionToUse.getTechUser();
        try {
            URI serverUri = url.toURI();
            client = new PDSClient(serverUri, credentials.getUserId(), credentials.getApiToken(), true);

        } catch (URISyntaxException e) {
            throw new WrongConfigurationException("URL not defined correct for local PDS solution: " + e.getMessage(), this);
        }

        LOG.info("Created PDS client for user: '{}', apiToken: '{}', solution: '{}'", client.getUsername(), "*".repeat(client.getSealedApiToken().length()),
                solutionToUse.getName());
        return client;
    }

    public boolean isLocalSecHubConfigured() {
        return configuration.getSetup().getLocal().isPresent();
    }

    public boolean isRemoteSecHubConfigured() {
        return configuration.getSetup().getRemote().isPresent();
    }

    public List<SecHubExecutorConfigDefinition> getLocalSecHubExecutorConfigurationsOrFail() {
        SecHubConfigurationDefinition config = getLocalSecHubConfigurationOrFail();
        return config.getExecutors();
    }

    public Set<String> createSetForLocalSecHubProfileIdsInExecutors() {
        Set<String> profileIds = new LinkedHashSet<>();

        for (SecHubExecutorConfigDefinition configDefinition : getLocalSecHubExecutorConfigurationsOrFail()) {
            profileIds.addAll(configDefinition.getProfiles());
        }
        return profileIds;
    }

    public Set<String> createSetForLocalSecHubProjectIdDefinitions() {

        SecHubConfigurationDefinition config = getLocalSecHubConfigurationOrFail();
        Set<String> projectIds = new LinkedHashSet<>();

        for (ProjectDefinition projectDefinition : config.getProjects().get()) {
            String projectName = projectDefinition.getName();
            projectIds.add(projectName);
        }
        return projectIds;
    }

    public List<PDSSolutionDefinition> getLocalPdsSolutionsOrFail() {
        return getLocalSetupOrFail().getPdsSolutions();

    }

    public PDSProductSetup getPDSProductSetupOrNull(String pdsProductId) {

        Collection<PDSServerConfiguration> serverConfigurations = getRuntimeMetaData().getPDSServerConfigurations();
        for (PDSServerConfiguration serverConfiguration : serverConfigurations) {
            for (PDSProductSetup product : serverConfiguration.getProducts()) {
                if (pdsProductId.equals(product.getId())) {
                    return product;
                }
            }
        }

        return null;
    }
    
    public PDSServerConfiguration getPDSServerConfigurationOrNull(String pdsProductId) {

        Collection<PDSServerConfiguration> serverConfigurations = getRuntimeMetaData().getPDSServerConfigurations();
        for (PDSServerConfiguration serverConfiguration : serverConfigurations) {
            for (PDSProductSetup product : serverConfiguration.getProducts()) {
                if (pdsProductId.equals(product.getId())) {
                    return serverConfiguration;
                }
            }
        }

        return null;
    }

    public ScanType getScanTypeForPdsProduct(String pdsProductId) {

        PDSProductSetup productSetup = getPDSProductSetupOrNull(pdsProductId);
        if (productSetup == null) {
            throw new WrongConfigurationException("There is no PDS server configuration file available, which references pds product id:" + pdsProductId, this);
        }
        return productSetup.getScanType();
    }

    public PDSSolutionDefinition getPDSSolutionDefinitionOrFail(String pdsProductId) {
        PDSServerConfiguration serverConfiguration = getPDSServerConfigurationOrNull(pdsProductId);
        if (serverConfiguration == null) {
            throw new WrongConfigurationException("No PDS server configuration found for pds product id:" + pdsProductId, this);
        }
        PDSSolutionDefinition result = getRuntimeMetaData().getPDSSolutionDefinition(serverConfiguration);
        if (result == null) {
            throw new WrongConfigurationException("No PDS solution definition found for pds product id:" + pdsProductId, this);
        }
        return result;
    }

    public PDSSolutionDefinition fetchPDSSolutionByProductIdOrFail(String pdsProductId) {
        PDSServerConfiguration serverConfig = getPDSServerConfigurationOrNull(pdsProductId);
        if (serverConfig==null) {
            throw new WrongConfigurationException("No PDS server configuration found for productId:"+pdsProductId,this);
        }
        PDSSolutionDefinition solutionDefinition = runtimeMetaData.getPDSSolutionDefinition(serverConfig);
        if (solutionDefinition==null) {
            throw new IllegalStateException("No PDS solution definition found for productId:"+pdsProductId + " but a server configuration was found?!?!");
        }
        return solutionDefinition;
    }

}
