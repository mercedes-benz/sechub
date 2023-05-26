package com.mercedesbenz.sechub.systemtest.runtime;

import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestFailure;

public class SystemTestRunResult {

    private String testName;
    private SystemTestFailure failure;

    SystemTestRunResult(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public SystemTestFailure getFailure() {
        return failure;
    }

    public void setFailure(SystemTestFailure failure) {
        this.failure = failure;
    }

    public boolean isFailed() {
        return failure != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TEST '");
        sb.append(testName).append("' ");

        if (isFailed()) {
            sb.append("[FAILED] - ");
            sb.append(failure.getMessage());
            sb.append("\nDetails:");
            sb.append(failure.getDetails());
        } else {
            sb.append("[ OK ]");
        }
        return sb.toString();
    }

}
