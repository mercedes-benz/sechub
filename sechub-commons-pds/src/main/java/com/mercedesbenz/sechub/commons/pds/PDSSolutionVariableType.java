// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

/**
 * The kind of variables wanted by PDS solution.
 *
 * @author Albert Tregnaghi
 *
 */
public enum PDSSolutionVariableType {

    MANDATORY_JOB_PARAMETER("Mandatory"),

    OPTIONAL_JOB_PARAMETER("Optional"),

    ;

    private String description;

    private PDSSolutionVariableType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
