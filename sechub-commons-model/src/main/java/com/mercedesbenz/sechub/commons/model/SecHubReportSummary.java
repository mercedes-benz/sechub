// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportSummary {
    
    public SecHubReportSummary() {
        
    }

    SecHubReportScanTypeSummary codeScan = new SecHubReportScanTypeSummary();
    SecHubReportScanTypeSummary infraScan = new SecHubReportScanTypeSummary();
    SecHubReportScanTypeSummary licenseScan = new SecHubReportScanTypeSummary();
    SecHubReportScanTypeSummary secretScan = new SecHubReportScanTypeSummary();
    SecHubReportScanTypeSummary webScan = new SecHubReportScanTypeSummary();

    public SecHubReportScanTypeSummary getCodeScan() {
        return codeScan;
    }

    public SecHubReportScanTypeSummary getInfraScan() {
        return infraScan;
    }

    public SecHubReportScanTypeSummary getWebScan() {
        return webScan;
    }

    public SecHubReportScanTypeSummary getLicenseScan() {
        return licenseScan;
    }

    public SecHubReportScanTypeSummary getSecretScan() {
        return secretScan;
    }

}
