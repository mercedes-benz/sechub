package com.mercedesbenz.sechub.systemtest.config;

public enum RuntimeVariable {

    WORKSPACE_ROOT("workspaceRoot", "Contains the absolute path to the workspace root folder"),

    CURRENT_TEST_FOLDER("currentTestFolder",
            "Contains the absolute path to the folder for the current test inside the workspace. This will be calculated at runtime.\n"
                    + "This variable can only be used in test preparation script definitions or in runSecHubJob definitions!"),

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
