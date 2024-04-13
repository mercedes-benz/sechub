// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.LinkedHashSet;
import java.util.Set;

public class SystemTestResult {

    private Set<SystemTestRunResult> runs = new LinkedHashSet<>();
    private Set<String> problems = new LinkedHashSet<>();

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

        if (hasProblems()) {
            sb.append("Problems detected:\n");
            for (String problem : getProblems()) {
                sb.append("- ");
                sb.append(problem);
                sb.append("\n");

            }
        }

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

    public boolean hasProblems() {
        return !problems.isEmpty();
    }

    public Set<String> getProblems() {
        return problems;
    }
}
