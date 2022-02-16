// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.logging;

/**
 * The type id is used for logging. So do NOT change the type id. We use the
 * type id to have possibility to refactor namings in code and avoid older logs
 * no longer be valid
 *
 * @author Albert Tregnaghi
 *
 */
public enum AlertLogReason {

    UNKNOWN("UNKNOWN"),

    CPU_OVERLOAD("CPU OVERLOAD"),

    MEMORY_OVERLOAD("MEMORY OVERLOAD"),;

    private String typeId;

    private AlertLogReason(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }
}
