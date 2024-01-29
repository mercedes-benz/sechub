// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class UserIdValidationImpl extends AbstractSimpleStringValidation implements UserIdValidation {

    public static final int USERNAME_LENGTH_MIN = 5;
    public static final int USERNAME_LENGTH_MAX = 40;

    @Override
    protected void setup(ValidationConfig config) {
        config.minLength = USERNAME_LENGTH_MIN;
        config.maxLength = USERNAME_LENGTH_MAX;
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
        return "user id validation";
    }
}
