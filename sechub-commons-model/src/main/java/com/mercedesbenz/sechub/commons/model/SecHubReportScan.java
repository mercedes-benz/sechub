// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportScan {

    private long total = 0;
    private long red = 0;
    private long yellow = 0;
    private long green = 0;
    private SecHubReportScanDetails details = new SecHubReportScanDetails();

    public void reportScanHelper(SecHubFinding finding) {
        incrementColors(finding);
        details.detailsHelper(finding);
    }

    public void incrementColors(SecHubFinding finding) {
        Severity severity = finding.getSeverity();
        switch (severity) {
        case HIGH -> incrementRedCount();
        case MEDIUM -> incrementYellowCount();
        case LOW, INFO -> incrementGreenCount();
        }
        incrementTotalCount();
    }

    protected void incrementRedCount() {
        this.red++;
    }

    protected void incrementYellowCount() {
        this.yellow++;
    }

    protected void incrementGreenCount() {
        this.green++;
    }

    protected void incrementTotalCount() {
        this.total++;
    }

    public long getTotal() {
        return total;
    }

    public long getRed() {
        return red;
    }

    public long getYellow() {
        return yellow;
    }

    public long getGreen() {
        return green;
    }

    public SecHubReportScanDetails getDetails() {
        return details;
    }
}
