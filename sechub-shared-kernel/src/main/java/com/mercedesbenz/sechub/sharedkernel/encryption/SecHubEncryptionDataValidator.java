package com.mercedesbenz.sechub.sharedkernel.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * A very simple validation - just checks that necessary fields are filled. For
 * NONE cipher algorithm we need no password data. For all others the password
 * source type and password source data must be defined to have no validation
 * errors.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class SecHubEncryptionDataValidator implements Validator {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubEncryptionDataValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return SecHubEncryptionData.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof SecHubEncryptionData data) {
            internalValidate(data, errors);
        } else {
            LOG.error("Validation cannot handle object type: {}", target == null ? null : target.getClass());
        }
    }

    private void internalValidate(SecHubEncryptionData data, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, SecHubEncryptionData.PROPERTY_ALGORITHM, "field.required");

        SecHubCipherAlgorithm algorithm = data.getAlgorithm();
        if (algorithm == null) {
            return;
        }
        switch (algorithm) {
        case NONE:
            // no password data necessary here
            break;
        default:
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, SecHubEncryptionData.PROPERTY_PASSWORD_SOURCETYPE, "field.required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, SecHubEncryptionData.PROPERTY_PASSWORD_SOURCEDATA, "field.required");
            break;

        }
    }
}
