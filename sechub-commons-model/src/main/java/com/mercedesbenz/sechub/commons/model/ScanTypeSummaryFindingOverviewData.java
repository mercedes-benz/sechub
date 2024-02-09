package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanTypeSummaryFindingOverviewData {
    private Integer cweId;
    private String name;
    private long count;

    public ScanTypeSummaryFindingOverviewData() {
        /* for serialization */
    }
    
    public ScanTypeSummaryFindingOverviewData(Integer cweId, String name) {
        this.cweId = cweId;
        this.name = name;
    }

    public void incrementCount() {
        this.count++;
    }

    public void setCweId(Integer cweId) {
        this.cweId = cweId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getCweId() {
        return cweId;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "ScanTypeSummaryFindingOverviewData [" + (cweId != null ? "cweId=" + cweId + ", " : "") + (name != null ? "name=" + name + ", " : "") + "count="
                + count + "]";
    }
}