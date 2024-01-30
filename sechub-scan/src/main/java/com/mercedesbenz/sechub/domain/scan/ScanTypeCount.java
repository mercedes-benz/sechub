// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.commons.model.ScanType;

public class ScanTypeCount implements Comparable<ScanTypeCount> {

    private ScanType scanType;
    protected long highSeverityCount;
    protected long mediumSeverityCount;
    protected long lowSeverityCount;

    private ScanTypeCount(ScanType scanType) {
        this.scanType = scanType;
        highSeverityCount = 0;
        mediumSeverityCount = 0;
        lowSeverityCount = 0;
    }

    public static ScanTypeCount of(ScanType scanType) {
        if (scanType == null) {
            throw new IllegalArgumentException("ScanType argument must exist");
        }
        return new ScanTypeCount(scanType);
    }

    public ScanType getScanType() {
        return scanType;
    }

    public long getHighSeverityCount() {
        return highSeverityCount;
    }

    public long getMediumSeverityCount() {
        return mediumSeverityCount;
    }

    public long getLowSeverityCount() {
        return lowSeverityCount;
    }

    public void incrementHighSeverityCount() {
        this.highSeverityCount++;
    }

    public void incrementMediumSeverityCount() {
        this.mediumSeverityCount++;
    }

    public void incrementLowSeverityCount() {
        this.lowSeverityCount++;
    }

    @Override
    public int compareTo(ScanTypeCount o) {
        if (o == null) {
            return 1;
        }
        String descriptionA = this.scanType.getDescription();
        String descriptionB = o.scanType.getDescription();
        return descriptionA.compareTo(descriptionB);
    }
}
