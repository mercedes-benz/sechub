// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents a result level - see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317648">SARIF
 * 2.1.0 specification entry</a>
 */
public enum Level {

    WARNING("warning"),

    ERROR("error"),

    NOTE("note"),

    NONE("none"),

    ;

    private String jsonValue;

    private Level(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue // serialize and deserialize
    public String getJsonValue() {
        return jsonValue;
    }

}
