// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.ClientCertificateConfiguration;
import com.mercedesbenz.sechub.commons.model.HTTPHeaderConfiguration;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationUsageByName;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;

/**
 * This builder creates a file structure data object for ONE scan type.
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubFileStructureDataProviderBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubFileStructureDataProviderBuilder.class);

    private ScanType scanType;

    private SecHubConfigurationModel model;

    private List<String> excludePatterns = new ArrayList<>();
    private List<String> includePatterns = new ArrayList<>();

    SecHubFileStructureDataProviderBuilder() {
    }

    public SecHubFileStructureDataProviderBuilder setScanType(ScanType type) {
        this.scanType = type;
        return this;
    }

    public SecHubFileStructureDataProviderBuilder setModel(SecHubConfigurationModel model) {
        this.model = model;
        return this;
    }

    public SecHubFileStructureDataProviderBuilder setExcludedFilePatterns(List<String> excludePatterns) {
        this.excludePatterns.clear();
        if (excludePatterns != null) {
            this.excludePatterns.addAll(excludePatterns);
        }
        LOG.debug("Exclude patterns set to {}", this.excludePatterns);
        return this;
    }

    public SecHubFileStructureDataProviderBuilder setIncludedFilePatterns(List<String> includePatterns) {
        this.includePatterns.clear();
        if (includePatterns != null) {
            this.includePatterns.addAll(includePatterns);
        }
        LOG.debug("Include patterns set to {}", this.includePatterns);
        return this;
    }

    public SecHubFileStructureDataProvider build() {
        if (scanType == null) {
            throw new IllegalStateException("scanType is not set");
        }

        if (model == null) {
            throw new IllegalStateException("model is not set");
        }

        MutableSecHubFileStructureDataProvider data = new MutableSecHubFileStructureDataProvider();
        data.addExcludeFilePatterns(excludePatterns);
        data.addIncludeFilePatterns(includePatterns);
        data.setScanType(scanType);

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
        case SECRET_SCAN:
            addAllUsages(data, model.getSecretScan(), true);
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
            Optional<SecHubWebScanApiConfiguration> apiOpt = webScan.getApi();
            addAllUsages(data, apiOpt, false);

            Optional<ClientCertificateConfiguration> clientCertOpt = webScan.getClientCertificate();
            addAllUsages(data, clientCertOpt, false);

            Optional<List<HTTPHeaderConfiguration>> httpHeaderConfigsOpt = webScan.getHeaders();
            if (httpHeaderConfigsOpt.isEmpty()) {
                break;
            }
            for (HTTPHeaderConfiguration httpHeaderConfig : httpHeaderConfigsOpt.get()) {
                Set<String> use = httpHeaderConfig.getNamesOfUsedDataConfigurationObjects();
                // To call Optional.ofNullable() only when it is needed
                if (use != null && !use.isEmpty()) {
                    addAllUsages(data, Optional.ofNullable(httpHeaderConfig), false);
                }
            }
            break;
        case ANALYTICS:

            data.setRootFolderAccepted(true);
            addAllUsages(data, model.getCodeScan(), false);
            addAllUsages(data, model.getLicenseScan(), false);
            break;
        case PREPARE:
            data.setRootFolderAccepted(true);
            addAllUsages(data, model.getCodeScan(), false);
            addAllUsages(data, model.getLicenseScan(), false);
            break;

        default:
            break;
        }

        return data;
    }

    private void addAllUsages(MutableSecHubFileStructureDataProvider data, Optional<? extends SecHubDataConfigurationUsageByName> scanDefinitionObject,
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
