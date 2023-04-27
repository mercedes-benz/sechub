// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.commons.model.ScanType;

public class ScanTypeCount {

    private ScanType scanType;
    private int highSeverityCount;
    private int mediumSeverityCount;
    private int lowSeverityCount;

    ScanTypeCount(ScanType scanType){
        this.scanType = scanType;
        highSeverityCount = 0;
        mediumSeverityCount = 0;
        lowSeverityCount = 0;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public int getHighSeverityCount() {
        return highSeverityCount;
    }

    public void setHighSeverityCount(int highSeverityCount) {
        this.highSeverityCount = highSeverityCount;
    }

    public int getMediumSeverityCount() {
        return mediumSeverityCount;
    }

    public void setMediumSeverityCount(int mediumSeverityCount) {
        this.mediumSeverityCount = mediumSeverityCount;
    }

    public int getLowSeverityCount() {
        return lowSeverityCount;
    }

    public void setLowSeverityCount(int lowSeverityCount) {
        this.lowSeverityCount = lowSeverityCount;
    }

    public void incrementHighSeverityCount(){
        this.highSeverityCount++;
    }

    public void incrementMediumSeverityCount(){
        this.mediumSeverityCount++;
    }

    public void incrementLowSeverityCount(){
        this.lowSeverityCount++;
    }

}
