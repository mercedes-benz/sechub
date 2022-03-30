// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationError.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mercedesbenz.sechub.commons.core.util.SimpleNetworkUtils;
import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;

public class SecHubConfigurationModelValidator {

    private static int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 80;

    private List<String> supportedVersions;

    public SecHubConfigurationModelValidator() {
        supportedVersions = new ArrayList<String>();

        // currently we only support 1.0 as version
        supportedVersions.add("1.0");
    }

    private String describeSupportedVersions() {
        return supportedVersions.toString();
    }

    private class InternalValidationContext {
        private Set<String> wellknownObjectNames = new HashSet<>();
        private SecHubConfigurationModelValidationResult result;
        private SecHubConfigurationModel model;;
    }

    public SecHubConfigurationModelValidationResult validate(SecHubConfigurationModel model) {
        SecHubConfigurationModelValidationResult result = new SecHubConfigurationModelValidationResult();
        InternalValidationContext context = new InternalValidationContext();
        context.result = result;
        context.model = model;
        validate(context);
        return result;
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
        handleDataConfiguration(context);
        handleScanConfigurations(context);
    }

    private void handleScanConfigurations(InternalValidationContext context) {
        if (!hasAtLeastOneScanConfiguration(context)) {
            context.result.addError(CONTAINS_NO_SCAN_CONFIGURATION);
        }
        handleCodeScanConfiguration(context);
        handleWebScanConfiguration(context);
        handleInfraScanConfiguration(context);

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
                        "The referenced name:'" + SimpleStringUtils.truncateWhenTooLong(name, MAX_NAME_LENGTH) + "' is not found in model");
            }
        }
    }

    private void handleWebScanConfiguration(InternalValidationContext context) {
        Optional<SecHubWebScanConfiguration> webScanOpt = context.model.getWebScan();
        if (!webScanOpt.isPresent()) {
            return;
        }

        SecHubWebScanConfiguration webScan = webScanOpt.get();
        URI uri = webScan.getUri();

        if (SimpleNetworkUtils.isURINullOrEmpty(uri)) {

            context.result.addError(WEB_SCAN_HAS_NO_URL_DEFINED);

        } else if (!SimpleNetworkUtils.isHttpProtocol(uri)) {

            String schema = SimpleStringUtils.truncateWhenTooLong(uri.getScheme(), 5);
            context.result.addError(WEB_SCAN_URL_HAS_UNSUPPORTED_SCHEMA, "Schema was: " + schema + " but supported is only HTTP/HTTPS");
        }

        handleOpenApi(context, webScan);

    }

    private void handleOpenApi(InternalValidationContext context, SecHubWebScanConfiguration webScan) {
        Optional<SecHubOpenAPIConfiguration> openApiopt = webScan.getOpenApi();
        if (!openApiopt.isPresent()) {
            return;
        }

        SecHubOpenAPIConfiguration openApi = openApiopt.get();
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

        validateNameUniquenAndNotNull(context, data.getSources());
        validateNameUniquenAndNotNull(context, data.getBinaries());

    }

    private void validateNameUniquenAndNotNull(InternalValidationContext context, Collection<? extends SecHubDataConfigurationObject> configurationObjects) {

        SecHubConfigurationModelValidationResult result = context.result;

        for (SecHubDataConfigurationObject configurationObject : configurationObjects) {
            String uniqueName = configurationObject.getUniqueName();
            if (uniqueName == null) {
                result.addError(DATA_CONFIG_OBJECT_NAME_IS_NULL);
                continue;
            }
            if (uniqueName.length() < MIN_NAME_LENGTH) {
                result.addError(DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_SMALL, "Name '" + uniqueName + "' lengh < " + MIN_NAME_LENGTH + " characters");
            }

            if (uniqueName.length() > MAX_NAME_LENGTH) {
                result.addError(DATA_CONFIG_OBJECT_NAME_LENGTH_TOO_BIG,
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

        return atLeastOne;
    }

    public class SecHubConfigurationModelValidationException extends Exception {

        private static final long serialVersionUID = 1L;

        public SecHubConfigurationModelValidationException(String message) {
            super(message);
        }

    }

}
