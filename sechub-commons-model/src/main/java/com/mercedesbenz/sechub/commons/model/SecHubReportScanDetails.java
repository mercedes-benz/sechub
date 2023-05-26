// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportScanDetails {
    private List<SeverityDetails> high = new ArrayList<>();
    private List<SeverityDetails> medium = new ArrayList<>();
    private List<SeverityDetails> low = new ArrayList<>();

    public void detailsHelper(SecHubFinding finding) {
        switch (finding.getSeverity()) {
        case HIGH -> detailsFiller(high, finding);
        case MEDIUM -> detailsFiller(medium, finding);
        case LOW, INFO -> detailsFiller(low, finding);
        }
    }

    private void detailsFiller(List<SeverityDetails> severityDetailsList, SecHubFinding finding) {
        boolean fl = false;
        int i = 0;
        while (fl == false && i < severityDetailsList.size()) {
            SeverityDetails details = severityDetailsList.get(i);
            if (details.getCweId().equals(finding.getCweId())) {
                details.incrementCount();
                fl = true;
            }
            i++;
        }
        if (fl == false) {
            severityDetailsList.add(new SeverityDetails(finding.getCweId(), finding.getName()));
        }
    }

    public List<SeverityDetails> getHigh() {
        return high;
    }

    public List<SeverityDetails> getMedium() {
        return medium;
    }

    public List<SeverityDetails> getLow() {
        return low;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class SeverityDetails {
        private Integer cweId;
        private String name;
        private long count;

        SeverityDetails(Integer cweId, String name) {
            this.cweId = cweId;
            this.name = name;
            this.count = 1;
        }

        public void incrementCount() {
            this.count++;
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
    }
}
