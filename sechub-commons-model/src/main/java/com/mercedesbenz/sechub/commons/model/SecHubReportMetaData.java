// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportMetaData {

    private Map<String, String> labels = new LinkedHashMap<>();
    private SecHubVersionControlData versionControl;

    private SecHubReportSummary summary = new SecHubReportSummary();

    public Map<String, String> getLabels() {
        return labels;
    }

    public SecHubReportSummary getSummary() {
        return summary;
    }

    public void setSummary(SecHubReportSummary summary) {
        this.summary = summary;
    }

    public Optional<SecHubVersionControlData> getVersionControl() {
        return Optional.ofNullable(versionControl);
    }

    public void setVersionControl(SecHubVersionControlData versionControl) {
        this.versionControl = versionControl;
    }

}
