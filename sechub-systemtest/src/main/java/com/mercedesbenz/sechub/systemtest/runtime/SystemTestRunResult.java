package com.mercedesbenz.sechub.systemtest.runtime;

import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestError;

public class SystemTestRunResult {

    private String testName;
    private SystemTestError error;

    SystemTestRunResult(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public SystemTestError getError() {
        return error;
    }

    public void setError(SystemTestError error) {
        this.error = error;
    }

    public boolean isFailed() {
        return error != null;
    }

}
