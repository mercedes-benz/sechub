// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

public enum SerecoSeverity {

    INFO,

    UNCLASSIFIED,

    LOW,

    MEDIUM,

    HIGH,

    CRITICAL,

    ;

    /**
     * Returns severity or <code>null</code> for given string.
     *
     * @param string value of the enumeration (is not case sensitive)
     * @return
     */
    public static SerecoSeverity fromString(String string) {
        if (string == null) {
            return null;
        }
        String upperCased = string.toUpperCase();
        for (SerecoSeverity severity : SerecoSeverity.values()) {
            if (severity.name().equals(upperCased)) {
                return severity;
            }
        }
        return null;
    }

}
