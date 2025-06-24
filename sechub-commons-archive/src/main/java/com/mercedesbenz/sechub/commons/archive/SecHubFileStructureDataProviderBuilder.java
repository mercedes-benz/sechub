// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.CommonConstants;
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
            data.setRootFolderAccepted(true); // for code scan we always accept root folder (legacy acceptance);
            addAllUsages(data, model.getCodeScan(), false);
            break;

        case INFRA_SCAN:
            break;

        case LICENSE_SCAN:
            addAllUsages(data, model.getLicenseScan(), true);
            break;
        case SECRET_SCAN:
            addAllUsages(data, model.getSecretScan(), true);

        case IAC_SCAN:
            addAllUsages(data, model.getIacScan(), true);
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
                addAllUsages(data, Optional.ofNullable(httpHeaderConfig), false);
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

    /*
     * Adds all usages of the given configUsageByName to the dataProvider. If
     * mustHave is true a runtime exception is thrown when no configUsageByName is
     * not present or names are empty.
     *
     */
    private void addAllUsages(MutableSecHubFileStructureDataProvider dataProvider, Optional<? extends SecHubDataConfigurationUsageByName> configUsageByName,
            boolean mustHave) {
        if (!configUsageByName.isPresent()) {
            if (mustHave) {
                new IllegalStateException("For scanType:" + scanType + " the configuration usage by name entry is missing.");
            }
            return;
        }

        SecHubDataConfigurationUsageByName usageByName = configUsageByName.get();

        Set<String> names = usageByName.getNamesOfUsedDataConfigurationObjects();
        if (names.isEmpty()) {
            if (mustHave) {
                new SecHubRuntimeException("Configuration file problem. For scanType:" + scanType + " at least one data configuration must be referenced");
            }
        }
        /*
         * Next lines will handle following scenario:
         *
         * When somebody has referenced the archive root explicit via a reserved archive
         * root identifier, we have to allow the root folder. The
         * "accepted reference names" are the names of folders which will be extracted
         * from__data__ section - because we simply allow the root folder for all root
         * archive reference IDs, we must not add the reserved identifiers additionally
         * this list.
         */
        Set<String> namesWithoutReserved = new HashSet<>(names);
        boolean rootAccepted = false;
        for (String reserved : CommonConstants.getAllRootArchiveReferenceIdentifiers()) {
            rootAccepted = namesWithoutReserved.remove(reserved) || rootAccepted;
        }

        if (rootAccepted) {
            /*
             * Only when root acceptance is calculated, we call "setRootFolderAccepted".
             * This is important, because there exists other logic which can also set root
             * folder as accepted - and we do NOT want to overwrite here with "false"!
             *
             */
            dataProvider.setRootFolderAccepted(true);
        }

        dataProvider.addAcceptedReferenceNames(namesWithoutReserved);
    }

}
