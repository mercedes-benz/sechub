// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

public class SecHubConfigurationModel {

    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_API_VERSION = "apiVersion";
    public static final String PROPERTY_WEB_SCAN = "webScan";
    public static final String PROPERTY_INFRA_SCAN = "infraScan";
    public static final String PROPERTY_CODE_SCAN = "codeScan";

    private Optional<SecHubWebScanConfiguration> webScan = Optional.empty();
    private Optional<SecHubInfrastructureScanConfiguration> infraScan = Optional.empty();
    private Optional<SecHubCodeScanConfiguration> codeScan = Optional.empty();

    private String apiVersion;

    private String projectId;

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setWebScan(SecHubWebScanConfiguration webScan) {
        this.webScan = Optional.ofNullable(webScan);
    }

    public Optional<SecHubWebScanConfiguration> getWebScan() {
        return webScan;
    }

    public void setCodeScan(SecHubCodeScanConfiguration codeScan) {
        this.codeScan = Optional.ofNullable(codeScan);
    }

    public Optional<SecHubCodeScanConfiguration> getCodeScan() {
        return codeScan;
    }

    public void setInfraScan(SecHubInfrastructureScanConfiguration infraStructureScan) {
        this.infraScan = Optional.ofNullable(infraStructureScan);
    }

    public Optional<SecHubInfrastructureScanConfiguration> getInfraScan() {
        return infraScan;
    }

}
