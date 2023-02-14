// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScanType {

    CODE_SCAN("codeScan"),

    WEB_SCAN("webScan"),

    INFRA_SCAN("infraScan"),

    LICENSE_SCAN("licenseScan"),

    /** not really a scan type but a report collector, only internally used */
    REPORT("report", true),

    /*
     * This is just a fallback for unknown scan type.
     */
    UNKNOWN("unknown", true),

    ;

    private String id;
    private boolean internalScanType;

    private ScanType(String id) {
        this(id, false);
    }

    private ScanType(String id, boolean internalScanType) {
        this.id = id;
        this.internalScanType = internalScanType;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    public boolean isInternalScanType() {
        return internalScanType;
    }
}
