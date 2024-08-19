// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.validation.AbstractValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationContext;

@Component
public class WebscanFalsePositiveProjectDataValidationImpl extends AbstractValidation<WebscanFalsePositiveProjectData>
        implements WebscanFalsePositiveProjectDataValidation {

    private static final String VALIDATOR_NAME = "webscan false positive project data validation";
    private static final String WILDCARD_ONLY_REGEX = "^[\\.\\*/:]+$";
    private static final Pattern WILDCARD_ONLY_PATTERN = Pattern.compile(WILDCARD_ONLY_REGEX);

    private static final int WEBSCAN_PROJECT_DATA_LIST_MAX_SIZE = 50;
    private static final int WEBSCAN_PROJECT_DATA_LIST_ENTRY_MAX_SIZE = 300;

    private static final String[] HOSTNAME_OR_IP_SEPARATORS = { ".", ":" };
    private static final String[] URL_PATH_SEPARATORS = { "/" };

    @Override
    protected void setup(AbstractValidation<WebscanFalsePositiveProjectData>.ValidationConfig config) {
        config.maxLength = WEBSCAN_PROJECT_DATA_LIST_ENTRY_MAX_SIZE; // we allow maximum 300 chars for list entries
    }

    @Override
    protected String getValidatorName() {
        return VALIDATOR_NAME;
    }

    @Override
    protected void validate(ValidationContext<WebscanFalsePositiveProjectData> context) {
        WebscanFalsePositiveProjectData webScan = getObjectToValidate(context);
        if (webScan == null) {
            return;
        }
        /* validate mandatory parts */
        validateCweId(context, webScan.getCweId());
        validateHostPatterns(context, webScan.getHostPatterns());
        validateUrlPathPatterns(context, webScan.getUrlPathPatterns());

        /* validate optional parts */
        validateMethods(context, webScan.getMethods());
        validatePorts(context, webScan.getPorts());
        validateProtocols(context, webScan.getProtocols());
    }

    private void validateCweId(ValidationContext<WebscanFalsePositiveProjectData> context, Integer cweId) {
        if (cweId == null || cweId >= 0) {
            return;
        }
        String name = "%s.%s".formatted(FalsePositiveProjectData.PROPERTY_WEBSCAN, WebscanFalsePositiveProjectData.PROPERTY_CWEID);
        context.addError(getValidatorName(),
                ": The value for '%s' must not be negative. Do not specify any CWE if the targeted finding has none or specify the correct value."
                        .formatted(name));
    }

    private void validateHostPatterns(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> hostPatterns) {
        String name = "%s.%s[]".formatted(FalsePositiveProjectData.PROPERTY_WEBSCAN, WebscanFalsePositiveProjectData.PROPERTY_HOSTPATTERNS);
        if (hostPatterns == null || hostPatterns.isEmpty()) {
            context.addError(getValidatorName(), ": The list of '%s' must contain at least one entry!".formatted(name));
            return;
        }
        validateSize(context, hostPatterns, name);

        // separators for host names, ipv4 addresses '.' and ipv6 addresses ':'
        validateRequirementsForMandatoryListWithWildcards(context, hostPatterns, name, HOSTNAME_OR_IP_SEPARATORS);
    }

    private void validateUrlPathPatterns(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> urlPathPatterns) {
        String name = "%s.%s[]".formatted(FalsePositiveProjectData.PROPERTY_WEBSCAN, WebscanFalsePositiveProjectData.PROPERTY_URLPATHPATTERNS);

        if (urlPathPatterns == null || urlPathPatterns.isEmpty()) {
            context.addError(getValidatorName(), ": The list of '%s' must contain at least one entry!".formatted(name));
            return;
        }
        validateSize(context, urlPathPatterns, name);

        // separator for url path patterns '/'
        validateRequirementsForMandatoryListWithWildcards(context, urlPathPatterns, name, URL_PATH_SEPARATORS);
    }

    private void validateMethods(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> methods) {
        String name = "%s.%s[]".formatted(FalsePositiveProjectData.PROPERTY_WEBSCAN, WebscanFalsePositiveProjectData.PROPERTY_METHODS);
        validateRequirementsForOptionalList(context, methods, name);
    }

    private void validatePorts(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> ports) {
        String name = "%s.%s[]".formatted(FalsePositiveProjectData.PROPERTY_WEBSCAN, WebscanFalsePositiveProjectData.PROPERTY_PORTS);
        validateRequirementsForOptionalList(context, ports, name);
    }

    private void validateProtocols(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> protocols) {
        String name = "%s.%s[]".formatted(FalsePositiveProjectData.PROPERTY_WEBSCAN, WebscanFalsePositiveProjectData.PROPERTY_PROTOCOLS);
        validateRequirementsForOptionalList(context, protocols, name);
    }

    private void validateRequirementsForMandatoryListWithWildcards(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> list, String name,
            String... allowedSeparators) {
        for (String entry : list) {
            if (entry.contains("\\")) {
                context.addError(getValidatorName(), ": Inside '%s' no backslashes are allowed!".formatted(name));
                continue;
            }
            if (WILDCARD_ONLY_PATTERN.matcher(name).matches()) {
                context.addError(getValidatorName(), ": Inside '%s' each element must consist of more than just wildcards!".formatted(name));
                continue;
            }
        }
    }

    private void validateRequirementsForOptionalList(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> list, String name) {
        if (list == null || list.isEmpty()) {
            return;
        }
        validateSize(context, list, name);
    }

    private void validateSize(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> list, String name) {
        validateMaxSize(context, list, WEBSCAN_PROJECT_DATA_LIST_MAX_SIZE, name);

        for (String entry : list) {
            validateMaxLength(context, entry, WEBSCAN_PROJECT_DATA_LIST_ENTRY_MAX_SIZE, ": Entry: '%s' inside '%s'".formatted(entry, name));
        }
    }

}
