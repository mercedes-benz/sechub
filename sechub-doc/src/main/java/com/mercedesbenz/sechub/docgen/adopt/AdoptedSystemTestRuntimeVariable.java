// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.adopt;

/**
 * This class is necessary to avoid build cycles from system tests when
 * generating system test parts. <br>
 * <br>
 * Avoids cycle:
 *
 * <pre>
 * sechub-doc->restdoc tests->java compile necesary ->  generates openapi3.json
 * sechub-systemtest --> sechub-api-java --> openApiGenerator --> openapi3.json
 * </pre>
 *
 * Equality to RuntimeVariable is checked by
 * <code>AdoptedSystemTestRuntimeVariableTest.java</code> If it fails, please
 * copy content system test RuntimeVariables at this location (class comments
 * are ignored means can be custom)
 *
 * @return
 */
public enum AdoptedSystemTestRuntimeVariable {

    /**
     * Folder for the current test
     */
    CURRENT_TEST_FOLDER("currentTestFolder",
            "Contains the absolute path to the folder for the current test inside the workspace. This will be calculated at runtime.\n"
                    + "This variable can only be used in test preparation script definitions or in runSecHubJob definitions!"),

    ADDITIONAL_RESOURCES_FOLDER("additionalResourcesFolder",
            "Containts the absolute path for a folder were additional resources can be found. For example a suite of preparation scripts which shall be used inside tests.\n"
                    + "If not explicit defined, the variable points to the location where the system tests has been started from");

    private String variableName;
    private String description;

    AdoptedSystemTestRuntimeVariable(String variableName, String description) {
        this.variableName = variableName;
        this.description = description;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getDescription() {
        return description;
    }

    public String asExpression() {
        return "${runtime." + variableName + "}";
    }

}
