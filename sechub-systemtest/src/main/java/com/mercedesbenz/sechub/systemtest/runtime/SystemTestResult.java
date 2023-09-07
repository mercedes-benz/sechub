// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.LinkedHashSet;
import java.util.Set;

public class SystemTestResult {

    private Set<SystemTestRunResult> runs = new LinkedHashSet<>();

    public Set<SystemTestRunResult> getRuns() {
        return runs;
    }

    public boolean hasFailedTests() {
        boolean hasErrors = false;
        for (SystemTestRunResult runResult : runs) {
            hasErrors = runResult.hasFailed();
            if (hasErrors) {
                break;
            }
        }
        return hasErrors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (SystemTestRunResult result : getRuns()) {
            sb.append(result.toString());
            sb.append("\n");
        }

        return sb.toString();
    }

    public int getAmountOfFailedTests() {
        int amount = 0;
        for (SystemTestRunResult run : runs) {
            if (run.hasFailed()) {
                amount++;
            }
        }
        return amount;
    }

    public int getAmountOfAllTests() {
        return runs.size();
    }
}
