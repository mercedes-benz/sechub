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

public class SystemTestRuntimeContext {

    private SystemTestConfiguration configuration;
    private SystemTestRunResult currentResult;
    private Set<SystemTestRunResult> results = new LinkedHashSet<>();

    private Map<PDSSolutionDefinition, PDSSolutionRuntimeData> pdsSolutionRuntimeDataMap = new LinkedHashMap<>();
    private boolean localRun;

    public void setCurrentResult(SystemTestRunResult currentResult) {
        this.currentResult = currentResult;
    }

    public SystemTestRunResult getCurrentResult() {
        return currentResult;
    }

    public SystemTestRuntimeContext(SystemTestConfiguration configuration, boolean localRun) {
        this.configuration = configuration;
        this.localRun = localRun;
    }

    public boolean isLocalRun() {
        return localRun;
    }

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
            throw WrongConfigurationException.buildException("To run a local system tests the local setpu must be configured!", configuration);
        }

        return localOpt.get();
    }

    public SecHubConfigurationDefinition getLocalSecHubConfigurationOrFail() {
        LocalSetupDefinition localSetup = getLocalSetupOrFail();
        LocalSecHubDefinition sechub = localSetup.getSecHub();
        SecHubConfigurationDefinition sechubConfig = sechub.getConfigure();
        return sechubConfig;
    }

}
