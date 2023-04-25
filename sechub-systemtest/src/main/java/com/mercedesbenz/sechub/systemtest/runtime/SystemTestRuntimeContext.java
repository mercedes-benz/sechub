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

class SystemTestRuntimeContext {

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

    void alterConfguration(SystemTestConfiguration configuration) {
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

    public void startNewRun(String runId) {
        this.currentResult = new SystemTestRunResult(runId);
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
            localAdminSecHubClient = createClient(localSecHub, localSecHub.getAdmin());

            LOG.info("Created local admin client for user: '{}', apiToken: '{}'", localAdminSecHubClient.getUsername(),
                    "*".repeat(localAdminSecHubClient.getSealedApiToken().length()));
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
            remoteUserSecHubClient = createClient(localSecHub, localSecHub.getAdmin());

            LOG.info("Created remote user client for user: {}, apiToken: '{}'", remoteUserSecHubClient.getUsername(),
                    "*".repeat(remoteUserSecHubClient.getSealedApiToken().length()));
        }
        return remoteUserSecHubClient;
    }

    private SecHubClient createClient(AbstractSecHubDefinition secHubDefinition, CredentialsDefinition credentials) {
        SecHubClient client = null;
        URL url = secHubDefinition.getUrl();
        if (url == null) {
            throw new WrongConfigurationException("URL not defined for local sechub server", this);
        }
        try {
            URI serverUri = url.toURI();
            client = new SecHubClient(serverUri, credentials.getUserId(), credentials.getApiToken(), true);

        } catch (URISyntaxException e) {
            throw new WrongConfigurationException("URL not defined correct local sechub server: " + e.getMessage(), this);
        }
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

}
