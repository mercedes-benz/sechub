// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScanType {

    CODE_SCAN("codeScan", "Code scan", "Scans the code for potential vulnerabilities (weaknesses). Also known as SAST or static source code analysis"),

    WEB_SCAN("webScan", "Web scan", "Scans a deployed web application for vulnerabilities. Also known as DAST."),

    INFRA_SCAN("infraScan", "Infra scan", "Scans infrastructure for vulnerabilities."),

    LICENSE_SCAN("licenseScan", "License scan", "Scans code or artifacts for license information"),

    SECRET_SCAN("secretScan", "Secret scan", "Scans code or artifacts for secrets"),

    REPORT("report", "Report", "Internal scan type for reporting", true),

    ANALYTICS("analytics", "Analytics", "Internal scan type for analytic phase", true),

    UNKNOWN("unknown", "Unknown", "Internal scan type for unknown types", true),

    ;

    private String id;
    private boolean internalScanType;
    private String description;
    private String text;

    private ScanType(String id, String text, String description) {
        this(id, text, description, false);
    }

    private ScanType(String id, String text, String description, boolean internalScanType) {
        this.id = id;
        this.text = text;
        this.internalScanType = internalScanType;
        this.description = description;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInternalScanType() {
        return internalScanType;
    }
}
