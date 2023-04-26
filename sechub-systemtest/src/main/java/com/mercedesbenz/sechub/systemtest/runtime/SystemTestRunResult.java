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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TEST '");
        sb.append(testName).append("' ");

        if (isFailed()) {
            sb.append("[FAILED] - ");
            sb.append(error.getMessage());
            sb.append("\nDetails:");
            sb.append(error.getDetails());
        } else {
            sb.append("[ OK ]");
        }
        return sb.toString();
    }

}
