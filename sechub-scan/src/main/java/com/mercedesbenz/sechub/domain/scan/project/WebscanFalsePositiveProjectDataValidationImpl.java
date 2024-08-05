// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.sharedkernel.validation.AbstractValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationContext;

@Component
public class WebscanFalsePositiveProjectDataValidationImpl extends AbstractValidation<WebscanFalsePositiveProjectData>
        implements WebscanFalsePositiveProjectDataValidation {

    private static final String WILDCARD_ONLY_REGEX = "^[\\.\\*/:]+$";
    private static final Pattern WILDCARD_ONLY_PATTERN = Pattern.compile(WILDCARD_ONLY_REGEX);

    private static final int WEBSCAN_PROJECT_DATA_LIST_MAX_SIZE = 50;
    private static final int WEBSCAN_PROJECT_DATA_LIST_ENTRY_MAX_SIZE = 300;

    @Override
    protected void setup(AbstractValidation<WebscanFalsePositiveProjectData>.ValidationConfig config) {
        config.maxLength = WEBSCAN_PROJECT_DATA_LIST_ENTRY_MAX_SIZE; // we allow maximum 300 chars for list entries
    }

    @Override
    protected String getValidatorName() {
        return "webscan false positive project data validation";
    }

    @Override
    protected void validate(ValidationContext<WebscanFalsePositiveProjectData> context) {
        WebscanFalsePositiveProjectData webScan = getObjectToValidate(context);
        if (webScan == null) {
            return;
        }
        /* validate mandatory parts */
        validateServers(context, webScan.getHostPatterns());
        validateUrlPatterns(context, webScan.getUrlPathPatterns());

        /* validate optional parts */
        validateCweId(context, webScan.getCweId());
        validateMethods(context, webScan.getMethods());
        validatePorts(context, webScan.getPorts());
        validateProtocols(context, webScan.getProtocols());
    }

    private void validateServers(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> servers) {
        String name = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_HOSTPATTERNS + "[]";
        if (servers == null || servers.isEmpty()) {
            context.addError(getValidatorName(), ": The list of '" + name + "' must contain at least one entry!");
            return;
        }
        validateRequirementsForOptionalList(context, servers, name);

        // separators for hostnames and ipv4 addresses '.' and ipv6 addresses ':'
        validateRequirementsForMandatoryListWithWildcards(context, servers, name, ".", ":");
    }

    private void validateUrlPatterns(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> urlPatterns) {
        String name = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_URLPATHPATTERNS + "[]";

        if (urlPatterns == null || urlPatterns.isEmpty()) {
            context.addError(getValidatorName(), ": The list of '" + name + "' must contain at least one entry!");
            return;
        }
        validateRequirementsForOptionalList(context, urlPatterns, name);

        // separator for url pattern '/'
        validateRequirementsForMandatoryListWithWildcards(context, urlPatterns, name, "/");
    }

    private void validateRequirementsForMandatoryListWithWildcards(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> list, String name,
            String... allowedSeparators) {
        for (String entry : list) {
            if (WILDCARD_ONLY_PATTERN.matcher(name).matches()) {
                context.addError(getValidatorName(), ": Inside '" + name + "' each element must consist of more than just wildcards!");
                continue;
            }

            String[] split = splitEntry(entry, allowedSeparators);
            if (split == null) {
                context.addError(getValidatorName(), ": Inside '" + name + "' expected separators: " + allowedSeparators);
            } else {
                boolean hasAtleastOneSectionWithoutWildcards = false;
                for (String sub : split) {
                    if (SimpleStringUtils.hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(sub, '-')) {
                        hasAtleastOneSectionWithoutWildcards = true;
                    }
                }
                if (!hasAtleastOneSectionWithoutWildcards) {
                    context.addError(getValidatorName(),
                            ": Entry: '" + entry + "' inside '" + name + " must contain at least one section without any wildcards.");
                }
            }
        }

    }

    private String[] splitEntry(String entry, String... separators) {
        for (String separator : separators) {
            if (!entry.contains(separator)) {
                continue;
            }
            return switch (separator) {
            case "." -> entry.split("\\.");
            case ":" -> entry.split(":");
            case "/" -> entry.split("/");
            default -> null;
            };
        }
        return null;
    }

    private void validateCweId(ValidationContext<WebscanFalsePositiveProjectData> context, Integer cweId) {
        if (cweId == null || cweId >= 0) {
            return;
        }
        String name = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_CWEID;
        if (cweId < 0) {
            context.addError(getValidatorName(), ": The value for '" + name
                    + "' must not be negative. Do not specify any CWE if the targeted finding has none or specify the correct value.");
        }
    }

    private void validateMethods(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> methods) {
        String name = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_METHODS + "[]";
        validateRequirementsForOptionalList(context, methods, name);
    }

    private void validatePorts(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> ports) {
        String name = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_PORTS + "[]";
        validateRequirementsForOptionalList(context, ports, name);
    }

    private void validateProtocols(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> protocols) {
        String name = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_PROTOCOLS + "[]";
        validateRequirementsForOptionalList(context, protocols, name);
    }

    private void validateRequirementsForOptionalList(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> list, String name) {
        if (list == null || list.isEmpty()) {
            return;
        }
        validateMaxSize(context, list, WEBSCAN_PROJECT_DATA_LIST_MAX_SIZE, name);

        for (String entry : list) {
            validateMaxLength(context, entry, WEBSCAN_PROJECT_DATA_LIST_ENTRY_MAX_SIZE, ": Entry: '" + entry + "' inside '" + name + "'");
        }
    }

}
