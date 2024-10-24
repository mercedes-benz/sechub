// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class TemplateIdValidationImpl extends AbstractSimpleStringValidation implements TemplateIdValidation {

    public static final int TEMPLATE_ID_LENGTH_MIN = 3;
    public static final int TEMPLATE_ID_LENGTH_MAX = 40;

    @Override
    protected void setup(ValidationConfig config) {
        config.minLength = TEMPLATE_ID_LENGTH_MIN;
        config.maxLength = TEMPLATE_ID_LENGTH_MAX;
    }

    @Override
    protected void validate(ValidationContext<String> context) {
        validateNotNull(context);
        if (context.isInValid()) {
            return;
        }
        validateNoUpperCaseCharacters(context);
        validateSameLengthWhenTrimmed(context);
        validateLength(context);

        validateOnlyAlphabeticDigitOrAllowedParts(context, '-', '_');
    }

    @Override
    protected String getValidatorName() {
        return "template id validation";
    }
}
