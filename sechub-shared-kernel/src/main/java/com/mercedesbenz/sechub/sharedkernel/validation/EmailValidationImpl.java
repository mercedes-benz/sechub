// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import java.util.List;

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

    private final List<EmailRule> emailRules;
    private final EmailValidator apacheEmailValidator = EmailValidator.getInstance();

    public EmailValidationImpl(List<EmailRule> emailRules) {
        this.emailRules = emailRules;
    }

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
        for (EmailRule emailRule : emailRules) {
            emailRule.applyRule(mail, context);
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
