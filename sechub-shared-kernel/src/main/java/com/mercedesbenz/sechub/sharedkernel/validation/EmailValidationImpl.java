// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

/**
 * Email validation implementation. Will check mail not null and in acceptable
 * format
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class EmailValidationImpl extends AbstractValidation<String> implements EmailValidation {

    EmailValidator apacheEmailValidator = EmailValidator.getInstance();

    @Override
    protected void setup(AbstractValidation<String>.ValidationConfig config) {

    }

    @Override
    protected void validate(ValidationContext<String> context) {
        validateNotNull(context);
        validateMail(context);
    }

    final protected void validateMail(ValidationContext<String> context) {
        String mail = context.objectToValidate;
        if (mail == null) {
            return;
        }
        if (!apacheEmailValidator.isValid(mail)) {
            addErrorMessage(context, "Mail is not in valid format");
        }
    }

    @Override
    protected String getValidatorName() {
        return "email validation";
    }

}
