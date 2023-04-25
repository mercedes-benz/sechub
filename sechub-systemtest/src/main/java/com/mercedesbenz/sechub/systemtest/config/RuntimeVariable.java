package com.mercedesbenz.sechub.systemtest.config;

public enum RuntimeVariable {

    WORKSPACE_ROOT("workspaceRoot", "Contains the absolute path to the workspace root folder"),

    PDS_SOLUTIONS_ROOT("pdsSolutionsRoot", "Contains the absolute path to the PDS solutions root folder"),

    ;

    private String variableName;
    private String description;

    RuntimeVariable(String variableName, String description) {
        this.variableName = variableName;
        this.description = description;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getDescription() {
        return description;
    }

}
