// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import static com.mercedesbenz.sechub.domain.administration.signup.SignupJsonInput.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.mercedesbenz.sechub.sharedkernel.validation.EmailValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.UserIdValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

import jakarta.annotation.PostConstruct;

@Component
public class SignupJsonInputValidator implements Validator {
    private static final Logger LOG = LoggerFactory.getLogger(SignupJsonInputValidator.class);

    @Autowired
    UserIdValidation useridValidation;

    @Autowired
    ApiVersionValidationFactory apiVersionValidationFactory;

    @Autowired
    EmailValidation emailValidation;

    private ApiVersionValidation apiVersionValidation;

    @PostConstruct
    void postConstruct() {
        apiVersionValidation = apiVersionValidationFactory.createValidationAccepting("1.0");
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return SignupJsonInput.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignupJsonInput selfRegistration = (SignupJsonInput) target;
        LOG.debug("Start validation for self registration of: {}", selfRegistration.getUserId());

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, PROPERTY_API_VERSION, "field.required");

        ValidationResult apiVersionValidationResult = apiVersionValidation.validate(selfRegistration.getApiVersion());
        if (!apiVersionValidationResult.isValid()) {
            errors.rejectValue(PROPERTY_API_VERSION, "api.error.unsupported.version", apiVersionValidationResult.getErrorDescription());
            return;
        }
        ValidationResult userIdValidationResult = useridValidation.validate(selfRegistration.getUserId());
        if (!userIdValidationResult.isValid()) {
            errors.rejectValue(PROPERTY_USER_ID, "api.error.registration.userid.invalid", userIdValidationResult.getErrorDescription());
            return;
        }

        ValidationResult emailValidationResult = emailValidation.validate(selfRegistration.getEmailAddress());
        if (!emailValidationResult.isValid()) {
            errors.rejectValue(PROPERTY_EMAIL_ADDRESS, "api.error.email.invalid", "Invalid email address");
            return;
        }
        LOG.debug("Selfregistration of {} was accepted", selfRegistration.getUserId());

    }

}
