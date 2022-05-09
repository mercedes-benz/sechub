package com.mercedesbenz.sechub.commons.archive;

import java.util.Optional;
import java.util.Set;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationUsageByName;
import com.mercedesbenz.sechub.commons.model.SecHubOpenAPIConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;

public class SecHubFileStructureConfigurationBuilder {

    private ScanType scanType;

    private SecHubConfigurationModel model;

    SecHubFileStructureConfigurationBuilder() {
    }

    public SecHubFileStructureConfigurationBuilder setScanType(ScanType type) {
        this.scanType = type;
        return this;
    }

    public SecHubFileStructureConfigurationBuilder setModel(SecHubConfigurationModel model) {
        this.model = model;
        return this;
    }

    public SecHubFileStructureConfiguration build() {
        if (scanType == null) {
            throw new IllegalStateException("scanType is not set");
        }

        if (model == null) {
            throw new IllegalStateException("model is not set");
        }

        SecHubFileStructureConfiguration data = new SecHubFileStructureConfiguration();

        switch (scanType) {
        case CODE_SCAN:
            data.setRootFolderAccepted(true);
            addAllUsages(data, model.getCodeScan(), false);
            break;
        case INFRA_SCAN:
            break;
        case LICENSE_SCAN:
            addAllUsages(data, model.getLicenseScan(), true);
            break;
        case REPORT:
            break;
        case UNKNOWN:
            break;
        case WEB_SCAN:
            Optional<SecHubWebScanConfiguration> webScanOpt = model.getWebScan();
            if (!webScanOpt.isPresent()) {
                throw new IllegalStateException("No webscan present but it is a " + scanType);
            }
            SecHubWebScanConfiguration webScan = webScanOpt.get();
            Optional<SecHubOpenAPIConfiguration> apiOpt = webScan.getOpenApi();
            addAllUsages(data, apiOpt, false);
            break;
        default:
            break;

        }

        return data;
    }

    private void addAllUsages(SecHubFileStructureConfiguration data, Optional<? extends SecHubDataConfigurationUsageByName> scanDefinitionObject,
            boolean mustHave) {
        if (!scanDefinitionObject.isPresent()) {
            if (mustHave) {
                new IllegalStateException("For scanType:" + scanType + " the configuration entry is missing.");
            }
            return;
        }

        SecHubDataConfigurationUsageByName usageByName = scanDefinitionObject.get();

        Set<String> names = usageByName.getNamesOfUsedDataConfigurationObjects();
        if (names.isEmpty()) {
            if (mustHave) {
                new SecHubRuntimeException("Confgiguration file problem. For scanType:" + scanType + " at least one data configuration must be referenced");
            }
        }
        data.addAcceptedReferenceNames(names);
    }

}
