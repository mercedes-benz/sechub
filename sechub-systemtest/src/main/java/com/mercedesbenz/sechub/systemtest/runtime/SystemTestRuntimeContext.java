package com.mercedesbenz.sechub.systemtest.runtime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mercedesbenz.sechub.systemtest.config.LocalSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;

class SystemTestRuntimeContext {

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

    /* only for tests */
    SystemTestRuntimeContext() {
    }

    public SystemTestRuntimeContext(SystemTestConfiguration originConfiguration, Path workspaceRoot, boolean localRun) {
        if (originConfiguration == null) {
            throw new IllegalArgumentException("Origin configuration may never be null!");
        }
        if (workspaceRoot == null) {
            throw new IllegalArgumentException("Workspace root may never be null!");
        }
        this.originConfiguration = originConfiguration;
        this.configuration = originConfiguration;

        this.localRun = localRun;
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
            throw new WrongConfigurationException("To run a local system tests the local setpu must be configured!", this);
        }

        return localOpt.get();
    }

    public SecHubConfigurationDefinition getLocalSecHubConfigurationOrFail() {
        LocalSetupDefinition localSetup = getLocalSetupOrFail();
        LocalSecHubDefinition sechub = localSetup.getSecHub();
        SecHubConfigurationDefinition sechubConfig = sechub.getConfigure();
        return sechubConfig;
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

}
