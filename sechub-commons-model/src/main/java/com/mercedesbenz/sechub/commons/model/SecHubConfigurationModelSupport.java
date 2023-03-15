// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecHubConfigurationModelSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubConfigurationModelSupport.class);

    public boolean isSourceRequired(ScanType scanType, SecHubConfigurationModel model) {
        return isRequired(scanType, model, SecHubDataConfigurationType.SOURCE);
    }

    public boolean isBinaryRequired(ScanType scanType, SecHubConfigurationModel model) {
        return isRequired(scanType, model, SecHubDataConfigurationType.BINARY);
    }

    private boolean isRequired(ScanType scanType, SecHubConfigurationModel model, SecHubDataConfigurationType dataType) {
        if (scanType == null) {
            throw new IllegalStateException("scanType is not set");
        }

        if (model == null) {
            throw new IllegalStateException("model is not set");
        }

        if (dataType == null) {
            throw new IllegalStateException("data type is not set");
        }

        switch (scanType) {
        case CODE_SCAN:
            return isDataTypeContainedOrReferenced(dataType, model, model.getCodeScan());
        case INFRA_SCAN:
            return false;
        case LICENSE_SCAN:
            return isDataTypeContainedOrReferenced(dataType, model, model.getLicenseScan());
        case REPORT:
            return false;
        case UNKNOWN:
            return false;
        case WEB_SCAN:
            Optional<SecHubWebScanConfiguration> webScanOpt = model.getWebScan();
            if (!webScanOpt.isPresent()) {
                return false;
            }
            SecHubWebScanConfiguration webScan = webScanOpt.get();
            Optional<SecHubWebScanApiConfiguration> apiOpt = webScan.getApi();
            return isDataTypeContainedOrReferenced(dataType, model, apiOpt);
        default:
            LOG.error("Unsupported scan type: {}", scanType);
            return false;

        }
    }

    private boolean isDataTypeContainedOrReferenced(SecHubDataConfigurationType dataType, SecHubConfigurationModel model,
            Optional<? extends SecHubDataConfigurationUsageByName> usageByNameOpt) {
        if (!usageByNameOpt.isPresent()) {
            LOG.debug("No usages found, so datatype {} not contained", dataType);
            return false;
        }
        SecHubDataConfigurationUsageByName usageByName = usageByNameOpt.get();
        if (usageByName instanceof SecHubCodeScanConfiguration) {
            if (SecHubDataConfigurationType.SOURCE.equals(dataType)) {
                SecHubCodeScanConfiguration scodeScanConfiguration = (SecHubCodeScanConfiguration) usageByName;
                if (scodeScanConfiguration.getFileSystem().isPresent()) {
                    LOG.debug("Source with embedded file system element found");
                    /*
                     * code scan has at least an embedded file system setup - so archive is
                     * necessary
                     */
                    return true;
                }
            }
        }
        Optional<SecHubDataConfiguration> dataOpt = model.getData();
        if (!dataOpt.isPresent()) {
            /* no data, no reference possible */
            LOG.debug("No data element found, so datatype {} not contained", dataType);
            return false;
        }
        Set<String> names = usageByName.getNamesOfUsedDataConfigurationObjects();
        if (names.isEmpty()) {
            LOG.debug("No names for data usages defined, so datatype {} not contained", dataType);
            return false;
        }
        SecHubDataConfiguration data = dataOpt.get();
        switch (dataType) {
        case BINARY:
            return atLeastOneNameReferencesOneElementInGivenDataConfiguration(names, data.getBinaries(), dataType);
        case SOURCE:
            return atLeastOneNameReferencesOneElementInGivenDataConfiguration(names, data.getSources(), dataType);
        default:
            LOG.error("Datatype {} unknown, so never contained", dataType);
            return false;

        }
    }

    private boolean atLeastOneNameReferencesOneElementInGivenDataConfiguration(Set<String> usageReferenceIds,
            List<? extends SecHubDataConfigurationObject> configurationObject, SecHubDataConfigurationType dataType) {
        if (configurationObject.isEmpty()) {
            LOG.debug("List of data elements for datatype {} is empty, so not contained", dataType);
            return false;
        }
        for (SecHubDataConfigurationObject dataConfigurationObject : configurationObject) {
            String name = dataConfigurationObject.getUniqueName();
            if (usageReferenceIds.contains(name)) {
                LOG.debug("List of data elements for datatype {} did contain {} from data, so contained", dataType, name);
                return true;
            }
        }
        LOG.debug("List of data elements for datatype {} did contain none of the referenced ones, so not contained", dataType);
        return false;
    }

}
