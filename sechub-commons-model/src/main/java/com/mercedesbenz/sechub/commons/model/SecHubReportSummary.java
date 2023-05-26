// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportSummary {

    SecHubReportScan codeScan = new SecHubReportScan();
    SecHubReportScan infraScan = new SecHubReportScan();
    SecHubReportScan webScan = new SecHubReportScan();

    public SecHubReportScan getCodeScan() {
        return codeScan;
    }

    public void setCodeScan(SecHubReportScan codeScan) {
        this.codeScan = codeScan;
    }

    public SecHubReportScan getInfraScan() {
        return infraScan;
    }

    public void setInfraScan(SecHubReportScan infraScan) {
        this.infraScan = infraScan;
    }

    public SecHubReportScan getWebScan() {
        return webScan;
    }

    public void setWebScan(SecHubReportScan webScan) {
        this.webScan = webScan;
    }
}
