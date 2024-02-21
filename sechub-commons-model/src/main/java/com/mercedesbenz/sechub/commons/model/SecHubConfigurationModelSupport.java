// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecHubConfigurationModelSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubConfigurationModelSupport.class);

    /**
     * Inspects the given model and checks if for given scan type source content is
     * available/required for the scan
     *
     * @param scanType
     * @param model
     * @return <code>true</code> when source reference found for given scan type
     */
    public boolean isSourceRequired(ScanType scanType, SecHubConfigurationModel model) {
        return isRequired(scanType, model, SecHubDataConfigurationType.SOURCE);
    }

    /**
     * Inspects the given model and checks if for given scan type binary content is
     * available/required for the scan
     *
     * @param scanType
     * @param model
     * @return <code>true</code> when binary reference found for given scan type
     */
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
            return isDataTypeContainedOrReferenced(dataType, model, model.getCodeScan(), SecHubCodeScanConfiguration.class);
        case INFRA_SCAN:
            return false;
        case LICENSE_SCAN:
            return isDataTypeContainedOrReferenced(dataType, model, model.getLicenseScan(), SecHubLicenseScanConfiguration.class);
        case SECRET_SCAN:
            return isDataTypeContainedOrReferenced(dataType, model, model.getSecretScan(), SecHubSecretScanConfiguration.class);
        case REPORT:
            return false;
        case ANALYTICS:
            boolean analyticsPossible =

                    isDataTypeContainedOrReferenced(dataType, model, model.getCodeScan(), SecHubCodeScanConfiguration.class)
                            || isDataTypeContainedOrReferenced(dataType, model, model.getLicenseScan(), SecHubLicenseScanConfiguration.class);

            return analyticsPossible;
        case UNKNOWN:
            return false;
        case WEB_SCAN:
            Optional<SecHubWebScanConfiguration> webScanOpt = model.getWebScan();
            if (!webScanOpt.isPresent()) {
                return false;
            }
            SecHubWebScanConfiguration webScan = webScanOpt.get();
            Optional<SecHubWebScanApiConfiguration> apiOpt = webScan.getApi();
            return isDataTypeContainedOrReferenced(dataType, model, apiOpt, SecHubWebScanApiConfiguration.class);

        case PREPARE:
            // TODO
            return true;

        default:
            LOG.error("Unsupported scan type: {}", scanType);
            return false;

        }
    }

    private <T extends SecHubDataConfigurationUsageByName> boolean isDataTypeContainedOrReferenced(SecHubDataConfigurationType dataType,
            SecHubConfigurationModel model, Optional<T> usageByNameOpt, Class<T> usageClazz) {
        if (!usageByNameOpt.isPresent()) {
            LOG.debug("No usages found, so datatype {} not contained. Usage clazz: {}", dataType, usageClazz);
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
        case NONE:
            return false;
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

    /**
     * Collects scan types
     *
     * @param model
     * @return set with scan types, never <code>null</code>
     */
    public Set<ScanType> collectPublicScanTypes(SecHubConfigurationModel model) {
        Set<ScanType> result = new LinkedHashSet<>();
        if (model.getCodeScan().isPresent()) {
            result.add(ScanType.CODE_SCAN);
        }
        if (model.getWebScan().isPresent()) {
            result.add(ScanType.WEB_SCAN);
        }
        if (model.getInfraScan().isPresent()) {
            result.add(ScanType.INFRA_SCAN);
        }
        if (model.getLicenseScan().isPresent()) {
            result.add(ScanType.LICENSE_SCAN);
        }
        if (model.getSecretScan().isPresent()) {
            result.add(ScanType.SECRET_SCAN);
        }
        return result;
    }

}
