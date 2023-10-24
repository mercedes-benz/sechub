// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

/**
 * These variables are calculated by system test engine at runtime. The
 * replacement is not directly exchanged after preparation (like done for
 * runtime variables) but only for dedicated situations.
 *
 * @author Albert Tregnaghi
 *
 */
public enum CalculatedVariables {

    /**
     * Represents the working directory for the current test
     */
    TEST_WORKING_DIRECTORY("testWorkingDirectory");

    private String variableName;

    CalculatedVariables(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public String asExpression() {
        return "${calculated." + getVariableName() + "}";
    }

}
