package com.mercedesbenz.sechub.commons.core.prepare;

public enum PrepareStatus {

    OK,

    FAILED,

    ;

    /**
     * Resolves status from given string
     *
     * @param value
     * @return status object or <code>null</code> if not found by value
     */
    public static PrepareStatus fromString(String value) {
        if (value != null) {
            for (PrepareStatus status : PrepareStatus.values()) {
                if (status.name().equalsIgnoreCase(value)) {
                    return status;
                }
            }
        }
        return null;
    }
}
