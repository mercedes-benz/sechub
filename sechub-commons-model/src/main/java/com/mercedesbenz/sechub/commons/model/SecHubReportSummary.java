// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportSummary {

    SecHubReportMetaDataSummary codeScan = new SecHubReportMetaDataSummary();
    SecHubReportMetaDataSummary infraScan = new SecHubReportMetaDataSummary();
    SecHubReportMetaDataSummary licenseScan = new SecHubReportMetaDataSummary();
    SecHubReportMetaDataSummary secretScan = new SecHubReportMetaDataSummary();
    SecHubReportMetaDataSummary webScan = new SecHubReportMetaDataSummary();

    public SecHubReportMetaDataSummary getCodeScan() {
        return codeScan;
    }

    public void setCodeScan(SecHubReportMetaDataSummary codeScan) {
        this.codeScan = codeScan;
    }

    public SecHubReportMetaDataSummary getInfraScan() {
        return infraScan;
    }

    public void setInfraScan(SecHubReportMetaDataSummary infraScan) {
        this.infraScan = infraScan;
    }

    public SecHubReportMetaDataSummary getWebScan() {
        return webScan;
    }

    public void setWebScan(SecHubReportMetaDataSummary webScan) {
        this.webScan = webScan;
    }

    public SecHubReportMetaDataSummary getLicenseScan() {
        return licenseScan;
    }

    public void setLicenseScan(SecHubReportMetaDataSummary licenseScan) {
        this.licenseScan = licenseScan;
    }

    public SecHubReportMetaDataSummary getSecretScan() {
        return secretScan;
    }

    public void setSecretScan(SecHubReportMetaDataSummary secretScan) {
        this.secretScan = secretScan;
    }

}
