// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubIacScanConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubWebScanConfiguration;

public class RunSecHubJobDefinition extends AbstractDefinition {

    private String project;

    private List<UploadDefinition> uploads = new ArrayList<>();

    private Optional<SecHubWebScanConfiguration> webScan = Optional.empty();
    private Optional<SecHubInfrastructureScanConfiguration> infraScan = Optional.empty();
    private Optional<SecHubCodeScanConfiguration> codeScan = Optional.empty();
    private Optional<SecHubLicenseScanConfiguration> licenseScan = Optional.empty();
    private Optional<SecHubSecretScanConfiguration> secretScan = Optional.empty();
    private Optional<SecHubIacScanConfiguration> iacScan = Optional.empty();

    private Optional<SecHubConfigurationMetaData> metaData = Optional.empty();

    public List<UploadDefinition> getUploads() {
        return uploads;
    }

    public void setProject(String projectName) {
        this.project = projectName;
    }

    public String getProject() {
        return project;
    }

    public Optional<SecHubWebScanConfiguration> getWebScan() {
        return webScan;
    }

    public void setWebScan(Optional<SecHubWebScanConfiguration> webScan) {
        this.webScan = webScan;
    }

    public Optional<SecHubInfrastructureScanConfiguration> getInfraScan() {
        return infraScan;
    }

    public void setInfraScan(Optional<SecHubInfrastructureScanConfiguration> infraScan) {
        this.infraScan = infraScan;
    }

    public Optional<SecHubCodeScanConfiguration> getCodeScan() {
        return codeScan;
    }

    public void setCodeScan(Optional<SecHubCodeScanConfiguration> codeScan) {
        this.codeScan = codeScan;
    }

    public Optional<SecHubLicenseScanConfiguration> getLicenseScan() {
        return licenseScan;
    }

    public void setLicenseScan(Optional<SecHubLicenseScanConfiguration> licenseScan) {
        this.licenseScan = licenseScan;
    }

    public Optional<SecHubSecretScanConfiguration> getSecretScan() {
        return secretScan;
    }

    public void setSecretScan(Optional<SecHubSecretScanConfiguration> secretScan) {
        this.secretScan = secretScan;
    }

    public void setIacScan(Optional<SecHubIacScanConfiguration> iacScan) {
        this.iacScan = iacScan;
    }

    public Optional<SecHubIacScanConfiguration> getIacScan() {
        return iacScan;
    }

    public Optional<SecHubConfigurationMetaData> getMetaData() {
        return metaData;
    }

    public void setMetaData(Optional<SecHubConfigurationMetaData> metaData) {
        this.metaData = metaData;
    }

}
