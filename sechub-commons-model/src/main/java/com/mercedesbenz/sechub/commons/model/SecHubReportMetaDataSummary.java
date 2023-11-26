// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportMetaDataSummary {

    private long total = 0;
    private long red = 0;
    private long yellow = 0;
    private long green = 0;
    private SecHubReportMetaDataSummaryDetails details = new SecHubReportMetaDataSummaryDetails();

    public void reportScanHelper(SecHubFinding finding) {
        incrementColors(finding);
        details.detailsHelper(finding);
    }

    protected void incrementColors(SecHubFinding finding) {
        Severity severity = finding.getSeverity();
        switch (severity) {
        case HIGH, CRITICAL -> incrementRedCount();
        case MEDIUM -> incrementYellowCount();
        case UNCLASSIFIED, INFO, LOW -> incrementGreenCount();
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

    public SecHubReportMetaDataSummaryDetails getDetails() {
        return details;
    }
}
