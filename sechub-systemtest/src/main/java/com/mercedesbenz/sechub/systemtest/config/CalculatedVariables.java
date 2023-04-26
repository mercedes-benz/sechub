package com.mercedesbenz.sechub.systemtest.config;

public enum CalculatedVariables {

    CURRENT_TEST_FOLDER("currentTestFolder");

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
