// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class MappingIdValidationImpl extends AbstractSimpleStringValidation implements MappingIdValidation {

    @Override
    protected void setup(AbstractValidation<String>.ValidationConfig config) {
        config.maxLength = 80;
        config.minLength = 5;
    }

    @Override
    protected void validate(ValidationContext<String> context) {
        validateNotNull(context);
        validateWithoutWhitespaces(context);
        validateOnlyAlphabeticDigitOrAllowedParts(context, '.', '-', '_');
        validateLength(context);

    }

    @Override
    protected String getValidatorName() {
        return "mapping id validation";
    }

}
