package com.mercedesbenz.sechub.systemtest.runtime.test;

import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestExecutionDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestFailure;
import com.mercedesbenz.sechub.systemtest.runtime.test.SystemTestRuntimeTestEngine.SecHubRunData;
import com.mercedesbenz.sechub.systemtest.runtime.variable.DynamicVariableCalculator;

public class TestEngineContext {

    private final SystemTestRuntimeTestEngine systemTestRuntimeTestEngine;
    private SecHubRunData secHubRunData;
    SystemTestRuntimeContext runtimeContext;
    TestDefinition test;
    private CurrentTestDynamicVariableCalculator dynamicVariableGenerator;

    TestEngineContext(SystemTestRuntimeTestEngine systemTestRuntimeTestEngine, TestDefinition test, SystemTestRuntimeContext runtimeContext) {
        this.systemTestRuntimeTestEngine = systemTestRuntimeTestEngine;
        this.test = test;
        this.runtimeContext = runtimeContext;
        this.dynamicVariableGenerator = new CurrentTestDynamicVariableCalculator(test, runtimeContext);

        appendSecHubRunData();
    }

    public DynamicVariableCalculator getDynamicVariableGenerator() {
        return dynamicVariableGenerator;
    }

    public TestDefinition getTest() {
        return test;
    }

    public boolean isSecHubTest() {
        return secHubRunData != null;
    }

    public SecHubRunData getSecHubRunData() {
        return secHubRunData;
    }

    public SystemTestRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    private void appendSecHubRunData() {
        TestExecutionDefinition execute = test.getExecute();
        Optional<RunSecHubJobDefinition> runSecHOptional = execute.getRunSecHubJob();
        if (runSecHOptional.isEmpty()) {
            return;
        }
        secHubRunData = this.systemTestRuntimeTestEngine.new SecHubRunData();
        RunSecHubJobDefinition runSecHubJobDefinition = runSecHOptional.get();

        SecHubConfigurationModel secHubConfiguration = this.systemTestRuntimeTestEngine.runSecHubJobTransformer
                .transformToSecHubConfiguration(runSecHubJobDefinition);

        secHubRunData.secHubConfiguration = secHubConfiguration;
    }

    public void markAsFailed(String message) {
        markAsFailed(message, null);
    }

    public void markAsFailed(String message, Exception e) {
        SystemTestRuntimeTestEngine.LOG.error("Test: {} failed: {}", getTest().getName(), message, e);

        SystemTestFailure failure = new SystemTestFailure();

        failure.setMessage("Test: " + getTest().getName() + " failed: " + message);
        if (e != null) {
            failure.setDetails(e.getMessage());
        }

        runtimeContext.getCurrentResult().setFailure(failure);
    }

}