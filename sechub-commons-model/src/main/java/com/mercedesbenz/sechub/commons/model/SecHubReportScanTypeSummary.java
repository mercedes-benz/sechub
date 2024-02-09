// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the report summary for one dedicated scan type.
 * Just a data representation
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportScanTypeSummary {

    private long total;

    private long red;
    private long yellow;
    private long green;

    private ScanTypeSummaryDetailData details = new ScanTypeSummaryDetailData();

    public ScanTypeSummaryDetailData getDetails() {
        return details;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getRed() {
        return red;
    }

    public void setRed(long red) {
        this.red = red;
    }

    public long getYellow() {
        return yellow;
    }

    public void setYellow(long yellow) {
        this.yellow = yellow;
    }

    public long getGreen() {
        return green;
    }

    public void setGreen(long green) {
        this.green = green;
    }
}
