// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils.*;
import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationError.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.mercedesbenz.sechub.commons.core.util.SimpleNetworkUtils;
import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;

public class SecHubConfigurationModelValidator {

    private static final String QUOTED_WEBSCAN_URL_WILDCARD_SYMBOL = Pattern.quote(SecHubWebScanConfiguration.WEBSCAN_URL_WILDCARD_SYMBOL);
    private static final Pattern PATTERN_QUOTED_WEBSCAN_URL_WILDCARD_SYMBOL = Pattern.compile(QUOTED_WEBSCAN_URL_WILDCARD_SYMBOL);

    private static int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 80;

    private static final int MIN_METADATA_LABEL_KEY_LENGTH = 1;
    private static final int MAX_METADATA_LABEL_KEY_LENGTH = 30;

    private static final int MAX_METADATA_LABEL_VALUE_LENGTH = 150;
    private static final int MAX_METADATA_LABEL_AMOUNT = 20;

    private static final int MAX_LIST_SIZE_INCLUDES = 500;
    private static final int MAX_LIST_SIZE_EXCLUDES = 500;
    private static final int MAX_LENGTH_PATH_SIZE = 2048;

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
            return;

        } else if (!SimpleNetworkUtils.isHttpProtocol(uri)) {

            String schema = SimpleStringUtils.truncateWhenTooLong(uri.getScheme(), 5);
            context.result.addError(WEB_SCAN_URL_HAS_UNSUPPORTED_SCHEMA, "Schema was: " + schema + " but supported is only HTTP/HTTPS");
            return;
        }

        handleIncludesAndExcludes(context, webScan);
        handleApi(context, webScan);
        handleHTTPHeaders(context, webScan);

    }

    private void handleIncludesAndExcludes(InternalValidationContext context, SecHubWebScanConfiguration webScan) {
        String targetUrl = webScan.getUrl().toString();
        WebScanConfigurationModelValidationContext webScanContext = new WebScanConfigurationModelValidationContext(context,
                addTrailingSlashToUrlWhenMissingAndLowerCase(targetUrl), Collections.emptyList());

        if (webScan.getExcludes().isPresent()) {
            List<String> excludes = webScan.getExcludes().get();
            validateExcludesOrIncludes(webScanContext, excludes, false);
        }
        if (webScan.getIncludes().isPresent()) {
            List<String> includes = webScan.getIncludes().get();
            validateExcludesOrIncludes(webScanContext, includes, true);
        }
    }

    private void handleApi(InternalValidationContext context, SecHubWebScanConfiguration webScan) {
        Optional<SecHubWebScanApiConfiguration> apiOpt = webScan.getApi();
        if (!apiOpt.isPresent()) {
            return;
        }

        SecHubWebScanApiConfiguration openApi = apiOpt.get();
        handleUsages(context, openApi);
    }

    private void handleHTTPHeaders(InternalValidationContext context, SecHubWebScanConfiguration webScan) {
        Optional<List<HTTPHeaderConfiguration>> optHttpHeaders = webScan.getHeaders();
        if (!optHttpHeaders.isPresent()) {
            return;
        }
        String targetUrl = webScan.getUrl().toString();
        WebScanConfigurationModelValidationContext webScanContext = new WebScanConfigurationModelValidationContext(context,
                addTrailingSlashToUrlWhenMissingAndLowerCase(targetUrl), optHttpHeaders.get());

        validateHeaderNamesAndValues(webScanContext);

        validateUrlsAreValid(webScanContext);

        validateHeaderOnlyForUrlNotDuplicated(webScanContext);
    }

    private void validateExcludesOrIncludes(WebScanConfigurationModelValidationContext webScanContext, List<String> urlList, boolean include) {
        String term = "excludes";
        SecHubConfigurationModelValidationError validationError = WEB_SCAN_EXCLUDE_INVALID;
        int maxListSize = MAX_LIST_SIZE_EXCLUDES;

        if (include) {
            term = "includes";
            validationError = WEB_SCAN_INCLUDE_INVALID;
            maxListSize = MAX_LIST_SIZE_INCLUDES;
        }

        if (urlList.size() > maxListSize) {
            webScanContext.markAsFailed(validationError, "A maximum of " + maxListSize + " " + term + " are allowed.");
            return;
        }

        for (String subUrlPattern : urlList) {
            if (subUrlPattern.length() > MAX_LENGTH_PATH_SIZE) {
                subUrlPattern = subUrlPattern.substring(0, MAX_LENGTH_PATH_SIZE);
                webScanContext.markAsFailed(validationError, "Maximum URL length is " + MAX_LENGTH_PATH_SIZE + " characters. The first " + MAX_LENGTH_PATH_SIZE
                        + " characters of the URL in question: " + subUrlPattern);
                return;
            }
            // we do not return if one include/exclude was wrong,
            // to be able to tell the user all wrong includes and excludes
            validateIncludeOrExcludePattern(webScanContext, subUrlPattern, include);
        }
    }

    private void validateIncludeOrExcludePattern(WebScanConfigurationModelValidationContext webScanContext, String subUrlPattern, boolean include) {
        if (subUrlPattern.contains("//")) {
            if (include) {
                webScanContext.markAsFailed(WEB_SCAN_INCLUDE_INVALID, "The include: " + subUrlPattern + " contains '//'!");
            } else {
                webScanContext.markAsFailed(WEB_SCAN_EXCLUDE_INVALID, "The exclude: " + subUrlPattern + " contains '//'!");
            }
            return;
        }

        String urlToCheck = webScanContext.sanatizedTargetUrl;
        if (subUrlPattern.startsWith("/")) {
            urlToCheck += subUrlPattern.substring(1);
        } else {
            urlToCheck += subUrlPattern;
        }

        String createdIncludeOrExcludeUrl = createUrlWithoutWildCards(urlToCheck);
        try {
            new URI(createdIncludeOrExcludeUrl).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            if (include) {
                webScanContext.markAsFailed(WEB_SCAN_INCLUDE_INVALID,
                        "The include: " + subUrlPattern + " does create an invalid URL without the wild cards : " + createdIncludeOrExcludeUrl);
            } else {
                webScanContext.markAsFailed(WEB_SCAN_EXCLUDE_INVALID,
                        "The exclude: " + subUrlPattern + " does create an invalid URL without the wild cards : " + createdIncludeOrExcludeUrl);
            }
        }
    }

    private void validateHeaderOnlyForUrlNotDuplicated(WebScanConfigurationModelValidationContext webScanContext) {
        if (webScanContext.failed) {
            return;
        }
        for (HTTPHeaderConfiguration sanatizedHttpHeader : webScanContext.sanatizedHttpHeaders) {
            if (sanatizedHttpHeader.getOnlyForUrls().isEmpty()) {
                continue;
            }
            for (String sanatizedOnlyForUrl : sanatizedHttpHeader.getOnlyForUrls().get()) {
                String headerUrlCombiniation = createHeaderUrlKey(sanatizedHttpHeader.getName(), sanatizedOnlyForUrl);
                if (webScanContext.headerUrlCombinations.add(headerUrlCombiniation) == false) {
                    webScanContext.markAsFailed(WEB_SCAN_NON_UNIQUE_HEADER_CONFIGURATION, "The header name is : " + sanatizedHttpHeader.getName());
                    return;
                }
            }
        }
    }

    private void validateHeaderNamesAndValues(WebScanConfigurationModelValidationContext webScanContext) {
        if (webScanContext.failed) {
            return;
        }
        for (HTTPHeaderConfiguration sanatizedHttpHeader : webScanContext.sanatizedHttpHeaders) {
            if (sanatizedHttpHeader.getName() == null || sanatizedHttpHeader.getName().isEmpty()) {
                webScanContext.markAsFailed(WEB_SCAN_NO_HEADER_NAME_DEFINED);
                return;
            }
            if (sanatizedHttpHeader.getValue() == null || sanatizedHttpHeader.getValue().isEmpty()) {
                webScanContext.markAsFailed(WEB_SCAN_NO_HEADER_VALUE_DEFINED, "The header name is : " + sanatizedHttpHeader.getName());
                return;
            }
        }
    }

    private void validateUrlsAreValid(WebScanConfigurationModelValidationContext webScanContext) {
        if (webScanContext.failed) {
            return;
        }
        for (HTTPHeaderConfiguration sanatizedHttpHeader : webScanContext.sanatizedHttpHeaders) {
            Optional<List<String>> onlyForUrls = sanatizedHttpHeader.getOnlyForUrls();
            if (onlyForUrls.isEmpty()) {
                continue;
            }
            for (String sanatizedOnlyForUrl : onlyForUrls.get()) {

                if (!sanatizedOnlyForUrl.contains(webScanContext.sanatizedTargetUrl)) {
                    webScanContext.markAsFailed(WEB_SCAN_HTTP_HEADER_ONLY_FOR_URL_DOES_NOT_CONTAIN_TARGET_URL,
                            "The header name is : " + sanatizedHttpHeader.getName());
                    return;
                }

                String sanatizedWithoutWildCards = createUrlWithoutWildCards(sanatizedOnlyForUrl);
                try {
                    new URI(sanatizedWithoutWildCards).toURL();
                } catch (URISyntaxException | MalformedURLException e) {
                    webScanContext.markAsFailed(WEB_SCAN_HTTP_HEADER_ONLY_FOR_URL_IS_NOT_A_VALID_URL,
                            "OnlyForUrls defined URL: " + sanatizedOnlyForUrl + " is not a valid URL.");
                }
            }
        }
    }

    private String createHeaderUrlKey(String headerNameLowerCased, String urlLowerCased) {
        String headerUrlId = headerNameLowerCased + ":" + urlLowerCased;
        return headerUrlId;
    }

    private String addTrailingSlashToUrlWhenMissingAndLowerCase(String url) {
        String resultUrl = url;
        if (!resultUrl.endsWith("/")) {
            resultUrl += "/";
        }
        return resultUrl.toLowerCase();
    }

    private String createLowerCasedUrlAndAddTrailingSlashIfMissing(String url) {
        // ensure "https://mywebapp.com/" and "https://mywebapp.com" are accepted as the
        // same. This way we can check if this URL contains our scan target URL.
        return addTrailingSlashToUrlWhenMissingAndLowerCase(url);
    }

    private String createUrlWithoutWildCards(String url) {
        return PATTERN_QUOTED_WEBSCAN_URL_WILDCARD_SYMBOL.matcher(url).replaceAll("");
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

    private class WebScanConfigurationModelValidationContext {
        private List<HTTPHeaderConfiguration> sanatizedHttpHeaders = new ArrayList<>();
        private boolean failed;
        private String sanatizedTargetUrl;

        // A Set to keep track of headers URL combinations
        // With this Set we validate the header configurations and inform the user about
        // invalid combinations
        private Set<String> headerUrlCombinations = new HashSet<>();
        private InternalValidationContext context;

        private WebScanConfigurationModelValidationContext(InternalValidationContext context, String sanatizedTargetUrl,
                List<HTTPHeaderConfiguration> httpHeaders) {
            this.context = context;
            this.sanatizedTargetUrl = sanatizedTargetUrl;
            initSanatizedHttpHeaders(httpHeaders);
        }

        private void initSanatizedHttpHeaders(List<HTTPHeaderConfiguration> httpHeaders) {
            for (HTTPHeaderConfiguration httpHeaderConfiguration : httpHeaders) {
                HTTPHeaderConfiguration sanatizedConfig = new HTTPHeaderConfiguration();
                String headerName = httpHeaderConfiguration.getName();
                if (headerName != null) {
                    sanatizedConfig.setName(headerName.toLowerCase());
                }
                sanatizedConfig.setValue(httpHeaderConfiguration.getValue());

                if (httpHeaderConfiguration.getOnlyForUrls().isPresent()) {
                    List<String> sanatizedOnlyForUrls = new ArrayList<>();
                    for (String onlyForUrl : httpHeaderConfiguration.getOnlyForUrls().get()) {
                        sanatizedOnlyForUrls.add(createLowerCasedUrlAndAddTrailingSlashIfMissing(onlyForUrl));
                    }
                    sanatizedConfig.setOnlyForUrls(Optional.of(sanatizedOnlyForUrls));
                } else {

                    String defaultWildCard = sanatizedTargetUrl + SecHubWebScanConfiguration.WEBSCAN_URL_WILDCARD_SYMBOL;
                    defaultWildCard = createLowerCasedUrlAndAddTrailingSlashIfMissing(defaultWildCard);
                    sanatizedConfig.setOnlyForUrls(Optional.of(Arrays.asList(defaultWildCard)));
                }
                this.sanatizedHttpHeaders.add(sanatizedConfig);
            }
        }

        public void markAsFailed(SecHubConfigurationModelValidationError error) {
            markAsFailed(error, null);
        }

        public void markAsFailed(SecHubConfigurationModelValidationError error, String message) {
            context.result.addError(error, message);
            failed = true;
        }

    }

}
