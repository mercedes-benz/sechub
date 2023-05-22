// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils.*;
import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationError.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mercedesbenz.sechub.commons.core.util.SimpleNetworkUtils;
import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;

public class SecHubConfigurationModelValidator {

    public static final int MAX_METADATA_LABEL_AMOUNT = 20;

    private static int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 80;

    private static final int MIN_METADATA_LABEL_KEY_LENGTH = 1;
    private static final int MAX_METADATA_LABEL_KEY_LENGTH = 30;

    private static final int MAX_METADATA_LABEL_VALUE_LENGTH = 150;

    SecHubConfigurationModelSupport modelSupport = new SecHubConfigurationModelSupport();

    private List<String> supportedVersions;

    /**
     * Does validate a given map containing meta data labels. Same logic as for
     * complete model, but useful when only label meta data shall be validated.
     *
     * @param metaDataLabels
     * @return validation result
     */
    public SecHubConfigurationModelValidationResult validateMetaDataLabels(Map<String, String> metaDataLabels) {
        SecHubConfigurationModelValidationResult result = new SecHubConfigurationModelValidationResult();
        handleMetaDataLabels(metaDataLabels, result);
        return result;
    }

    public SecHubConfigurationModelValidator() {
        supportedVersions = new ArrayList<String>();

        // currently we only support 1.0 as version
        supportedVersions.add("1.0");
    }

    /**
     * Validates a complete model
     *
     * @param model
     * @return validation result
     */
    public SecHubConfigurationModelValidationResult validate(SecHubConfigurationModel model) {
        SecHubConfigurationModelValidationResult result = new SecHubConfigurationModelValidationResult();
        InternalValidationContext context = new InternalValidationContext();
        context.result = result;
        context.model = model;
        validate(context);
        return result;
    }

    private String describeSupportedVersions() {
        return supportedVersions.toString();
    }

    private class InternalValidationContext {
        private Set<String> wellknownObjectNames = new HashSet<>();
        private SecHubConfigurationModelValidationResult result;
        private SecHubConfigurationModel model;;
    }

    private void handleMetaDataLabels(Map<String, String> labels, SecHubConfigurationModelValidationResult result) {
        Set<String> keySet = labels.keySet();
        /* validate max amount of labels */
        if (keySet.size() > MAX_METADATA_LABEL_AMOUNT) {
            result.addError(METADATA_TOO_MANY_LABELS);
            return;
        }

        /* validate keys */
        for (String key : keySet) {
            if (key == null || key.length() < MIN_METADATA_LABEL_KEY_LENGTH) {
                result.addError(METADATA_LABEL_KEY_TOO_SHORT);
                return;
            }
            if (key.length() > MAX_METADATA_LABEL_KEY_LENGTH) {
                result.addError(METADATA_LABEL_KEY_TOO_LONG);
                return;
            }
            if (!hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(key, '-', '_', '.')) {
                result.addError(METADATA_LABEL_KEY_CONTAINS_ILLEGAL_CHARACTERS,
                        "Label key '" + key + "' may only contain 'a-z','0-9', '-', '_' or '.' characters");
                continue;
            }
        }

        /* validate values */
        for (String value : labels.values()) {
            if (value == null) {
                continue;// we accept even null values
            }
            if (value.length() > MAX_METADATA_LABEL_VALUE_LENGTH) {
                result.addError(METADATA_LABEL_VALUE_TOO_LONG);
                return;
            }
        }
    }

    private void validate(InternalValidationContext context) {
        if (context.model == null) {
            context.result.addError(MODEL_NULL);
            return;
        }
        String apiVersion = context.model.getApiVersion();
        if (apiVersion == null) {
            context.result.addError(API_VERSION_NULL);
        } else if (!supportedVersions.contains(apiVersion)) {
            context.result.addError(API_VERSION_NOT_SUPPORTED, "Supported versions are:" + describeSupportedVersions());
            return;
        }
        handleScanTypesAndModuleGroups(context);
        handleDataConfiguration(context);
        handleScanConfigurations(context);
        handleMetaData(context);
    }

    private void handleMetaData(InternalValidationContext context) {
        Optional<SecHubConfigurationMetaData> metaDataOpt = context.model.getMetaData();
        if (metaDataOpt.isEmpty()) {
            return;
        }

        SecHubConfigurationMetaData metaData = metaDataOpt.get();
        Map<String, String> labels = metaData.getLabels();
        handleMetaDataLabels(labels, context.result);

    }

    private void handleScanTypesAndModuleGroups(InternalValidationContext context) {
        Set<ScanType> scanTypes = modelSupport.collectPublicScanTypes(context.model);
        handleScanTypes(context, scanTypes);

        handleModuleGroup(context, scanTypes);
    }

    private void handleModuleGroup(InternalValidationContext context, Set<ScanType> scanTypes) {
        ModuleGroup group = ModuleGroup.resolveModuleGroupOrNull(scanTypes);
        if (group != null) {
            /* no problems, the matching module group can be found */
            return;
        }
        Map<ScanType, ModuleGroup> moduleGroupDetectionMap = new LinkedHashMap<>();
        /* we can have two reasons here: no group at all or multiple groups */
        for (ModuleGroup groupToInspect : ModuleGroup.values()) {
            for (ScanType scanType : scanTypes) {
                if (groupToInspect.isGivenModuleInGroup(scanType)) {
                    moduleGroupDetectionMap.put(scanType, groupToInspect);
                }

            }
        }
        Collection<ModuleGroup> detectedModuleGroups = moduleGroupDetectionMap.values();
        if (detectedModuleGroups.isEmpty()) {
            context.result.addError(NO_MODULE_GROUP_DETECTED);
        } else {
            context.result.addError(MULTIPLE_MODULE_GROUPS_DETECTED, "Module groups detected: " + detectedModuleGroups);
        }
    }

    private void handleScanTypes(InternalValidationContext context, Set<ScanType> scanTypes) {
        if (scanTypes.isEmpty()) {
            context.result.addError(NO_PUBLIC_SCAN_TYPES_DETECTED);
        }
    }

    private void handleScanConfigurations(InternalValidationContext context) {
        if (!hasAtLeastOneScanConfiguration(context)) {
            context.result.addError(CONTAINS_NO_SCAN_CONFIGURATION);
        }
        handleCodeScanConfiguration(context);
        handleWebScanConfiguration(context);
        handleInfraScanConfiguration(context);
        handleLicenseScanConfiguration(context);
        handleSecretScanConfiguration(context);

    }

    private void handleLicenseScanConfiguration(InternalValidationContext context) {
        Optional<SecHubLicenseScanConfiguration> licenseScanOpt = context.model.getLicenseScan();

        if (!licenseScanOpt.isPresent()) {
            return;
        }
        SecHubDataConfigurationUsageByName licenseScan = licenseScanOpt.get();

        if (licenseScan.getNamesOfUsedDataConfigurationObjects().isEmpty()) {
            context.result.addError(NO_DATA_CONFIG_SPECIFIED_FOR_SCAN);
        }

        handleUsages(context, licenseScan);
    }

    private void handleSecretScanConfiguration(InternalValidationContext context) {
        Optional<SecHubSecretScanConfiguration> secretScanOpt = context.model.getSecretScan();

        if (!secretScanOpt.isPresent()) {
            return;
        }
        SecHubDataConfigurationUsageByName secretScan = secretScanOpt.get();

        if (secretScan.getNamesOfUsedDataConfigurationObjects().isEmpty()) {
            context.result.addError(NO_DATA_CONFIG_SPECIFIED_FOR_SCAN);
        }

        handleUsages(context, secretScan);
    }

    private void handleCodeScanConfiguration(InternalValidationContext context) {
        Optional<SecHubCodeScanConfiguration> codeScanOpt = context.model.getCodeScan();
        if (!codeScanOpt.isPresent()) {
            return;
        }
        SecHubDataConfigurationUsageByName codeScan = codeScanOpt.get();
        handleUsages(context, codeScan);

    }

    private void handleUsages(InternalValidationContext context, SecHubDataConfigurationUsageByName usageByName) {
        Set<String> names = usageByName.getNamesOfUsedDataConfigurationObjects();
        for (String name : names) {
            if (!context.wellknownObjectNames.contains(name)) {
                context.result.addError(REFERENCED_DATA_CONFIG_OBJECT_NAME_NOT_EXISTING,
                        "The referenced name: '" + SimpleStringUtils.truncateWhenTooLong(name, MAX_NAME_LENGTH) + "' is not found in model");
            }
        }
    }

    private void handleWebScanConfiguration(InternalValidationContext context) {
        Optional<SecHubWebScanConfiguration> webScanOpt = context.model.getWebScan();
        if (!webScanOpt.isPresent()) {
            return;
        }

        SecHubWebScanConfiguration webScan = webScanOpt.get();
        URI uri = webScan.getUrl();

        if (SimpleNetworkUtils.isURINullOrEmpty(uri)) {

            context.result.addError(WEB_SCAN_HAS_NO_URL_DEFINED);

        } else if (!SimpleNetworkUtils.isHttpProtocol(uri)) {

            String schema = SimpleStringUtils.truncateWhenTooLong(uri.getScheme(), 5);
            context.result.addError(WEB_SCAN_URL_HAS_UNSUPPORTED_SCHEMA, "Schema was: " + schema + " but supported is only HTTP/HTTPS");
        }

        handleApi(context, webScan);

    }

    private void handleApi(InternalValidationContext context, SecHubWebScanConfiguration webScan) {
        Optional<SecHubWebScanApiConfiguration> apiOpt = webScan.getApi();
        if (!apiOpt.isPresent()) {
            return;
        }

        SecHubWebScanApiConfiguration openApi = apiOpt.get();
        handleUsages(context, openApi);
    }

    private void handleInfraScanConfiguration(InternalValidationContext context) {
        Optional<SecHubInfrastructureScanConfiguration> infraScanOpt = context.model.getInfraScan();
        if (!infraScanOpt.isPresent()) {
            return;
        }
        SecHubInfrastructureScanConfiguration infraScan = infraScanOpt.get();
        if (infraScan.getUris().isEmpty() && infraScan.getIps().isEmpty()) {
            context.result.addError(INFRA_SCAN_HAS_NO_URIS_OR_IPS_DEFINED);
        }

    }

    private void handleDataConfiguration(InternalValidationContext context) {
        Optional<SecHubDataConfiguration> dataOpt = context.model.getData();
        if (!dataOpt.isPresent()) {
            return;
        }

        SecHubDataConfiguration data = dataOpt.get();

        validateNameUniqueAndNotNull(context, data.getSources());
        validateNameUniqueAndNotNull(context, data.getBinaries());

    }

    private void validateNameUniqueAndNotNull(InternalValidationContext context, Collection<? extends SecHubDataConfigurationObject> configurationObjects) {

        SecHubConfigurationModelValidationResult result = context.result;

        for (SecHubDataConfigurationObject configurationObject : configurationObjects) {
            String uniqueName = configurationObject.getUniqueName();
            if (uniqueName == null) {
                result.addError(DATA_CONFIG_OBJECT_NAME_IS_NULL);
                continue;
            }
            if (!hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(uniqueName, '-', '_')) {
                result.addError(DATA_CONFIG_OBJECT_NAME_CONTAINS_ILLEGAL_CHARACTERS,
                        "Name '" + uniqueName + "' may only contain 'a-z','0-9', '-' or '_' characters");
                continue;
            }

            if (uniqueName.length() < MIN_NAME_LENGTH) {
                result.addError(DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_SHORT, "Name '" + uniqueName + "' lengh < " + MIN_NAME_LENGTH + " characters");
            }

            if (uniqueName.length() > MAX_NAME_LENGTH) {
                result.addError(DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_LONG,
                        "Name '" + SimpleStringUtils.truncateWhenTooLong(uniqueName, MAX_NAME_LENGTH) + "' length > " + MAX_NAME_LENGTH + " characters");
            }

            if (context.wellknownObjectNames.contains(uniqueName)) {
                result.addError(DATA_CONFIG_OBJECT_NAME_IS_NOT_UNIQUE, "A configuration object with name " + uniqueName + " is already defined");
            }

            context.wellknownObjectNames.add(uniqueName);
        }

    }

    private boolean hasAtLeastOneScanConfiguration(InternalValidationContext context) {
        boolean atLeastOne = false;
        SecHubConfigurationModel model = context.model;
        if (model == null) {
            return false;
        }
        atLeastOne = atLeastOne || model.getCodeScan().isPresent();
        atLeastOne = atLeastOne || model.getInfraScan().isPresent();
        atLeastOne = atLeastOne || model.getWebScan().isPresent();
        atLeastOne = atLeastOne || model.getLicenseScan().isPresent();
        atLeastOne = atLeastOne || model.getSecretScan().isPresent();

        return atLeastOne;
    }

    public class SecHubConfigurationModelValidationException extends Exception {

        private static final long serialVersionUID = 1L;

        public SecHubConfigurationModelValidationException(String message) {
            super(message);
        }

    }

}
