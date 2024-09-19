// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the report summary for one dedicated scan type. Just a data
 * representation
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportScanTypeSummary {

    private long total;

    private long critical;
    private long high;
    private long medium;
    private long low;
    private long unclassified;
    private long info;

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

    public long getCritical() {
        return critical;
    }

    public void setCritical(long critical) {
        this.critical = critical;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }

    public long getMedium() {
        return medium;
    }

    public void setMedium(long medium) {
        this.medium = medium;
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }

    public long getUnclassified() {
        return unclassified;
    }

    public void setUnclassified(long unclassified) {
        this.unclassified = unclassified;
    }

    public long getInfo() {
        return info;
    }

    public void setInfo(long info) {
        this.info = info;
    }

    public void incrementCritical() {
        this.critical++;
    }

    public void incrementHigh() {
        this.high++;
    }

    public void incrementMedium() {
        this.medium++;
    }

    public void incrementLow() {
        this.low++;
    }

    public void incrementUnclassified() {
        this.unclassified++;
    }

    public void incrementInfo() {
        this.info++;
    }

}
