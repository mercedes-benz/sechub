// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

public enum ZapURLType {
    INCLUDE("include"),

    EXCLUDE("exclude"),

    ;

    private String id;

    private ZapURLType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
