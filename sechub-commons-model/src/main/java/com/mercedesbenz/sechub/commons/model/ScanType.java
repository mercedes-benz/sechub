// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScanType {

    CODE_SCAN("codeScan", "Scans the code for potential vulnerabilities (weaknesses). Also known as SAST or static source code analysis"),

    WEB_SCAN("webScan", "Scans a deployed web application for vulnerabilities. Also known as DAST."),

    INFRA_SCAN("infraScan", "Scans infrastructure for vulnerabilities."),

    LICENSE_SCAN("licenseScan", "Scans code or artifacts for license information"),

    SECRET_SCAN("secretScan", "Scans code or artifacts for secrets"),

    REPORT("report", "Internal scan type for reporting", true),

    ANALYTICS("analytics", "Internal scan type for analytic phase", true),

    PREPARE("prepare", "Internal scan type for prepare phase", true),

    UNKNOWN("unknown", "Internal scan type for unknown types", true),

    ;

    private String id;
    private boolean internalScanType;
    private String description;

    private ScanType(String id, String description) {
        this(id, description, false);
    }

    private ScanType(String id, String description, boolean internalScanType) {
        this.id = id;
        this.internalScanType = internalScanType;
        this.description = description;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInternalScanType() {
        return internalScanType;
    }
}
