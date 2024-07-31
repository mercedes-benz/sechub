// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanTypeSummaryDetailData {

    private List<ScanTypeSummaryFindingOverviewData> critical = new ArrayList<>();
    private List<ScanTypeSummaryFindingOverviewData> high = new ArrayList<>();
    private List<ScanTypeSummaryFindingOverviewData> medium = new ArrayList<>();
    private List<ScanTypeSummaryFindingOverviewData> low = new ArrayList<>();
    private List<ScanTypeSummaryFindingOverviewData> unclassified = new ArrayList<>();
    private List<ScanTypeSummaryFindingOverviewData> info = new ArrayList<>();

    public List<ScanTypeSummaryFindingOverviewData> getCritical() {
        return critical;
    }

    public List<ScanTypeSummaryFindingOverviewData> getHigh() {
        return high;
    }

    public List<ScanTypeSummaryFindingOverviewData> getMedium() {
        return medium;
    }

    public List<ScanTypeSummaryFindingOverviewData> getLow() {
        return low;
    }

    public List<ScanTypeSummaryFindingOverviewData> getUnclassified() {
        return unclassified;
    }

    public List<ScanTypeSummaryFindingOverviewData> getInfo() {
        return info;
    }

}
