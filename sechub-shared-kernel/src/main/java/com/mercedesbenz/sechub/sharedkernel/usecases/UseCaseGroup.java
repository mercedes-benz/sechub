// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.usecases;

public enum UseCaseGroup {

    ANONYMOUS("Anonymous", "All these usecases handling anonymous access."),

    USER_ADMINISTRATION("User administration", "Usecases handling administration of users"),

    PROJECT_ADMINISTRATION("Project administration", "Usecases for project administration"),

    USER_SELF_SERVICE("User self service", "User actions belonging to their user identity"),

    SECHUB_EXECUTION("Sechub execution", "Execution of SecHub -either by CLI or direct with REST api call"),

    SIGN_UP("Sign up", "All these usecases are handling user sign up (part of user self registration process)"),

    JOB_ADMINISTRATION("Job administration", "Usecases about job administration"),

    TECHNICAL("Technical", "Usecases about technical operations being executed by sechub itself"),

    TESTING("Testing", "Some use cases for testing"),

    CONFIGURATION("Configuration", "Usecases for configuration parts"),

    ENCRYPTION("Encryption", "Usecases for encryption parts"),

    OTHER("Other", "All other use cases"),

    ;

    private String description;
    private String title;

    private UseCaseGroup(String title, String description) {
        this.description = description;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}