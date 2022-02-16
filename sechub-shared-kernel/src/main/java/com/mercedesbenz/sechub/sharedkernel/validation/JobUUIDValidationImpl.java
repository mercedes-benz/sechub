// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * This validation validates only for null, because every UUID is correct
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class JobUUIDValidationImpl extends AbstractValidation<UUID> implements JobUUIDValidation {

    @Override
    protected void setup(AbstractValidation<UUID>.ValidationConfig config) {

    }

    @Override
    protected void validate(ValidationContext<UUID> context) {
        validateNotNull(context);
    }

    @Override
    protected String getValidatorName() {
        return "job UUID validation";
    }

}
