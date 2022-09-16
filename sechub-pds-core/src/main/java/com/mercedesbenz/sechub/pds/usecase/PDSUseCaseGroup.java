// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.usecase;

public enum PDSUseCaseGroup {

    ANONYMOUS("Anonymous", "All these usecases handling anonymous access."),

    JOB_EXECUTION("Job execution", "Execution of PSD jobs"),

    MONITORING("Monitoring", "Monitoring usecases"),

    AUTO_CLEANUP("Auto cleanup", "Usecases about auto cleanup operations"),

    OTHER("Other", "All other use cases"),

    ;

    private String description;
    private String title;

    private PDSUseCaseGroup(String title, String description) {
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