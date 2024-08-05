// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.validation.AbstractSimpleStringValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.AbstractValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationContext;

@Component
public class FalsePositiveProjectDataIdValidationImpl extends AbstractSimpleStringValidation implements FalsePositiveProjectDataIdValidation {

    private static final int PROJECT_DATA_ID_MIN_SIZE = 1;
    private static final int PROJECT_DATA_ID_MAX_SIZE = 100;

    @Override
    protected void setup(AbstractValidation<String>.ValidationConfig config) {
        config.maxLength = PROJECT_DATA_ID_MAX_SIZE; // we allow maximum 100 chars for ids
        config.minLength = PROJECT_DATA_ID_MIN_SIZE; // we allow minimum 1 char for ids, since it is mandatory
    }

    @Override
    protected void validate(ValidationContext<String> context) {
        validateNotNull(context);
        validateLength(context);
        validateOnlyAlphabeticDigitOrAllowedParts(context, '-', '_');
    }

    @Override
    protected String getValidatorName() {
        return "false positive project data id validation";
    }
}
