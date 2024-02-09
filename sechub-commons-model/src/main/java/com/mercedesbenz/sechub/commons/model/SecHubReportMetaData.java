// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportMetaData {

    private Map<String, String> labels = new LinkedHashMap<>();

    private SecHubReportSummary summary = new SecHubReportSummary();

    public Map<String, String> getLabels() {
        return labels;
    }

    public SecHubReportSummary getSummary() {
        return summary;
    }

}
