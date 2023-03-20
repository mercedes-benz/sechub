package com.mercedesbenz.sechub.systemtest.runtime;

import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestError;

public class SystemTestRunResult {

    private String runIdentifier;
    private SystemTestError error;

    SystemTestRunResult(String id) {
        this.runIdentifier = id;
    }

    public String getRunIdentifier() {
        return runIdentifier;
    }

    public SystemTestError getError() {
        return error;
    }

    public void setError(SystemTestError error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }

}
