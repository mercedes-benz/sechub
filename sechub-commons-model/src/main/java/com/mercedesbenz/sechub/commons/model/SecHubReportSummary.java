// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubReportSummary {

    private Optional<SecHubReportScanTypeSummary> codeScan = Optional.ofNullable(null);
    private Optional<SecHubReportScanTypeSummary> infraScan = Optional.ofNullable(null);
    private Optional<SecHubReportScanTypeSummary> licenseScan = Optional.ofNullable(null);
    private Optional<SecHubReportScanTypeSummary> secretScan = Optional.ofNullable(null);
    private Optional<SecHubReportScanTypeSummary> webScan = Optional.ofNullable(null);

    public SecHubReportSummary() {
    }

    public Optional<SecHubReportScanTypeSummary> getCodeScan() {
        return codeScan;
    }

    public Optional<SecHubReportScanTypeSummary> getInfraScan() {
        return infraScan;
    }

    public Optional<SecHubReportScanTypeSummary> getWebScan() {
        return webScan;
    }

    public Optional<SecHubReportScanTypeSummary> getLicenseScan() {
        return licenseScan;
    }

    public Optional<SecHubReportScanTypeSummary> getSecretScan() {
        return secretScan;
    }

    public void setCodeScan(SecHubReportScanTypeSummary codeScan) {
        this.codeScan = Optional.ofNullable(codeScan);
    }

    public void setInfraScan(SecHubReportScanTypeSummary infraScan) {
        this.infraScan = Optional.ofNullable(infraScan);
    }

    public void setLicenseScan(SecHubReportScanTypeSummary licenseScan) {
        this.licenseScan = Optional.ofNullable(licenseScan);
    }

    public void setSecretScan(SecHubReportScanTypeSummary secretScan) {
        this.secretScan = Optional.ofNullable(secretScan);
    }

    public void setWebScan(SecHubReportScanTypeSummary webScan) {
        this.webScan = Optional.ofNullable(webScan);
    }

}
