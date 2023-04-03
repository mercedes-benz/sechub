package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    EnvironmentProvider environmentProvider;
    LocationSupport locationSupport;

    private SystemTestConfiguration configuration;
    private SystemTestRunResult currentResult;
    private Set<SystemTestRunResult> results = new LinkedHashSet<>();
    private SystemTestRuntimeMetaData runtimeMetaData = new SystemTestRuntimeMetaData();
    private Map<PDSSolutionDefinition, PDSSolutionRuntimeData> pdsSolutionRuntimeDataMap = new LinkedHashMap<>();

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

    public SystemTestRuntimeContext(SystemTestConfiguration originConfiguration, boolean localRun) {
        this.originConfiguration = originConfiguration;
        this.configuration = originConfiguration;

        this.localRun = localRun;
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

}
