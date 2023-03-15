// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

/**
 * Sha256 checksum validation implementation.
 *
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class Sha256ChecksumValidationImpl extends AbstractValidation<String> implements Sha256ChecksumValidation {

    @Override
    protected void setup(AbstractValidation<String>.ValidationConfig config) {
        config.minLength = 64;
        config.maxLength = config.minLength;
    }

    @Override
    protected void validate(ValidationContext<String> context) {
        validateNotNull(context);
        validateMinLength(context, context.objectToValidate, getMinLength(), "sha256");
        validateMaxLength(context, context.objectToValidate, getMaxLength(), "sha256");
        validateSha256(context);
    }

    final protected void validateSha256(ValidationContext<String> context) {
        String sha256 = context.objectToValidate;
        if (sha256 == null) {
            return;
        }
        for (char c : sha256.toLowerCase().toCharArray()) {
            if (Character.isDigit(c)) {
                continue;
            }
            switch (c) {
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                /* everything fine */
                continue;
            }
            addErrorMessage(context, "Given checksum is not a valid hex encoded sha256 checksum");
            return;
        }
    }

    @Override
    protected String getValidatorName() {
        return "sha256 checksum validation";
    }

}
