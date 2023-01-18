// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScanType {

    CODE_SCAN("codeScan"),

    WEB_SCAN("webScan"),

    INFRA_SCAN("infraScan"),

    LICENSE_SCAN("licenseScan"),

    /**
     * not really a scan type but for report collectors, only internally used - not
     * for users
     */
    REPORT("report"),

    /**
     * not really a scan type but for analytics, only internally used - not for
     * users
     */
    ANALYTICS("analytics"),

    /*
     * This is just a fallback for unknown scan type.
     */
    UNKNOWN("unknown"),

    ;

    private String id;

    private ScanType(String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }
}
