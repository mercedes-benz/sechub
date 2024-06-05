// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.job;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

/**
 * Represents the execution state of a scheduled SecHub job.
 * 
 * Attention: never change the enum values because they are used for persistence
 * as identifiers!
 * 
 * @author Albert Tregnaghi
 *
 */
@MustBeKeptStable
public enum ExecutionState {

    INITIALIZING("Initializing. E.g. Workspace has pending uploads etc."),

    READY_TO_START("No state information available"),

    STARTED("Is started"),

    CANCEL_REQUESTED("A cancel was requested - but not ended now"),

    CANCELED("The job has been canceled"),

    /*
     * TODO 2024-06-05 de-jcup, winzj: write an integration test with sechub client
     * to check if go client can run with the new enum part
     */
    PAUSED("The job has been paused and can be resumed by another SecHub instance where scheduler is running"),

    ENDED("Has ended - with failure or success");

    private String description;

    private ExecutionState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Resolves execution state from given string - if not found it will return
     * <code>null</code>
     *
     * @param string
     * @return result or <code>null</code>
     */
    public static ExecutionState fromString(String string) {
        for (ExecutionState result : values()) {
            if (result.name().equalsIgnoreCase(string)) {
                return result;
            }
        }
        return null;
    }
}
