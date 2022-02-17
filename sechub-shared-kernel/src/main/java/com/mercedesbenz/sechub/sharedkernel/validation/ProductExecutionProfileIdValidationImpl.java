// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class ProductExecutionProfileIdValidationImpl extends AbstractSimpleStringValidation implements ProductExecutionProfileIdValidation {

    public static final int PROJECTID_LENGTH_MIN = 3;
    public static final int PROJECTID_LENGTH_MAX = 30;

    @Override
    protected void setup(ValidationConfig config) {
        config.minLength = PROJECTID_LENGTH_MIN;
        config.maxLength = PROJECTID_LENGTH_MAX;
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
        return "profile id validation";
    }
}
