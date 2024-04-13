// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.job;

/**
 * Represents the execution state of a scheduled SecHub job
 *
 * @author Albert Tregnaghi
 *
 */
public enum ExecutionState {

    INITIALIZING("Initializing. E.g. Workspace has pending uploads etc."),

    READY_TO_START("No state information available"),

    STARTED("Is started"),

    CANCEL_REQUESTED("A cancel was requested - but not ended now"),

    CANCELED("The job has been canceled"),

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
