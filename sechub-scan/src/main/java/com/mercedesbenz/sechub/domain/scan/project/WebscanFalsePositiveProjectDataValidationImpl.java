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
    private static final String WILDCARD_AND_URL_SEPARATOR_REGEX = "^[\\.\\*/:]+$";
    private static final Pattern WILDCARD_AND_URL_SEPARATOR_PATTERN = Pattern.compile(WILDCARD_AND_URL_SEPARATOR_REGEX);

    private static final int WEBSCAN_PROJECT_DATA_LIST_MAX_SIZE = 30;
    private static final int WEBSCAN_PROJECT_DATA_LIST_ENTRY_MAX_SIZE = 40;

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
        validateUrlPattern(context, webScan.getUrlPattern());

        /* validate optional parts */
        validateMethods(context, webScan.getMethods());
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

    private void validateUrlPattern(ValidationContext<WebscanFalsePositiveProjectData> context, String urlPattern) {
        String name = "%s.%s".formatted(FalsePositiveProjectData.PROPERTY_WEBSCAN, WebscanFalsePositiveProjectData.PROPERTY_URLPATTERN);
        if (urlPattern == null || urlPattern.isBlank()) {
            context.addError(getValidatorName(), ": The '%s' is mandatory and must not be empty!".formatted(name));
            return;
        }
        if (urlPattern.contains("\\")) {
            context.addError(getValidatorName(), ": Inside '%s' no backslashes are allowed!".formatted(name));
        }
        if (WILDCARD_AND_URL_SEPARATOR_PATTERN.matcher(name).matches()) {
            context.addError(getValidatorName(), ": The '%s': '%s' each element must consist of more than just wildcards!".formatted(name, urlPattern));
        }
    }

    private void validateMethods(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> methods) {
        String name = "%s.%s[]".formatted(FalsePositiveProjectData.PROPERTY_WEBSCAN, WebscanFalsePositiveProjectData.PROPERTY_METHODS);
        if (methods == null || methods.isEmpty()) {
            return;
        }
        validateSize(context, methods, name);
    }

    private void validateSize(ValidationContext<WebscanFalsePositiveProjectData> context, List<String> list, String name) {
        validateMaxSize(context, list, WEBSCAN_PROJECT_DATA_LIST_MAX_SIZE, name);

        for (String entry : list) {
            validateMaxLength(context, entry, WEBSCAN_PROJECT_DATA_LIST_ENTRY_MAX_SIZE, ": Entry: '%s' inside '%s'".formatted(entry, name));
        }
    }

}
