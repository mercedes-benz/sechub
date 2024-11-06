// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class AssetFileNameValidationImpl extends AbstractSimpleStringValidation implements AssetFileNameValidation {

    public static final int FILENAME_LENGTH_MIN = 2;
    public static final int FILENAME_LENGTH_MAX = 100;

    @Override
    protected void setup(ValidationConfig config) {
        config.minLength = FILENAME_LENGTH_MIN;
        config.maxLength = FILENAME_LENGTH_MAX;
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
        return "asset filename validation";
    }
}
