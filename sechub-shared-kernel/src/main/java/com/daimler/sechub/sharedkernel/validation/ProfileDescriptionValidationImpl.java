// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class ProfileDescriptionValidationImpl extends AbstractSimpleStringValidation implements ProfileDescriptionValidation {

    @Override
    protected void setup(AbstractValidation<String>.ValidationConfig config) {
        config.maxLength = 170;// we got 512 characters inside database /3 (UTF8)= 170
    }

    @Override
    protected void validate(ValidationContext<String> context) {
        if (getObjectToValidate(context)==null) {
            /* we accept null*/
            return;
        }
        validateMaxLength(context);
    }

    @Override
    protected String getValidatorName() {
        return "profile description validation";
    }

}
