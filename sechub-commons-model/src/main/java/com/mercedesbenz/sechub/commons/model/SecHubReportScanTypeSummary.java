// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the report summary for one dedicated scan type
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportScanTypeSummary {

    private long total = 0;

    private long red = 0;
    private long yellow = 0;
    private long green = 0;

    private ScanTypeSummaryDetailData details = new ScanTypeSummaryDetailData();

    /**
     * Adds finding data to calculated values. Be aware: There is no duplication check - If you add
     * the same finding multiple times color counts, details etc. will increased multiple times!
     * 
     * @param finding the finding to add
     */
    public void addToCalculation(SecHubFinding finding) {

        incrementColorCounts(finding);

        details.addToCalculation(finding);
    }

    protected void incrementColorCounts(SecHubFinding finding) {
        Severity severity = finding.getSeverity();

        switch (severity) {
        case HIGH, CRITICAL -> red++;
        case MEDIUM -> yellow++;
        case UNCLASSIFIED, INFO, LOW -> green++;
        }
        total++;
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

    public ScanTypeSummaryDetailData getDetails() {
        return details;
    }
}
