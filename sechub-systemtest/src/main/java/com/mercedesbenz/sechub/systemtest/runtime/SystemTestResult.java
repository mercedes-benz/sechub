package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.LinkedHashSet;
import java.util.Set;

public class SystemTestResult {

    public Set<SystemTestRunResult> runs = new LinkedHashSet<>();

    public Set<SystemTestRunResult> getRuns() {
        return runs;
    }
}
