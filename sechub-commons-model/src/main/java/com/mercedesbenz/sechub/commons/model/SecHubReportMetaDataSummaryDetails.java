// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportMetaDataSummaryDetails {

    Map<String, SeverityDetails> high = new TreeMap<>();
    Map<String, SeverityDetails> medium = new TreeMap<>();
    Map<String, SeverityDetails> low = new TreeMap<>();

    public void detailsHelper(SecHubFinding finding) {
        switch (finding.getSeverity()) {
        case HIGH -> detailsFiller(high, finding);
        case MEDIUM -> detailsFiller(medium, finding);
        case LOW, INFO -> detailsFiller(low, finding);
        }
    }

    protected void detailsFiller(Map<String, SeverityDetails> helperMap, SecHubFinding finding) {
        Integer cweId = finding.getCweId();
        String name = finding.getName();
        SeverityDetails severityDetails = helperMap.get(name);
        if (severityDetails != null) {
            severityDetails.incrementCount();
        } else {
            helperMap.put(name, new SeverityDetails(cweId, name));
        }
    }

    public List<SeverityDetails> getHigh() {
        return new ArrayList<>(high.values());
    }

    public List<SeverityDetails> getMedium() {
        return new ArrayList<>(medium.values());
    }

    public List<SeverityDetails> getLow() {
        return new ArrayList<>(low.values());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected class SeverityDetails {
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
