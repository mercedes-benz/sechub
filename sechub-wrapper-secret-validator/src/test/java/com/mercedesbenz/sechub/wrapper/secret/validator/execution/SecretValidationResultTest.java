// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SecretValidationResultTest {

    @ParameterizedTest
    @EnumSource(SecretValidationStatus.class)
    void setting_a_new_value_results_in_the_specified_value_being_set(SecretValidationStatus status) {
        /* prepare */
        SecretValidationResult validationResult = new SecretValidationResult();

        /* execute */
        validationResult.setValidationStatus(status);

        /* test */
        assertEquals(status, validationResult.getValidationStatus());

    }

    @Test
    void setting_null_results_in_the_default_value_staying_set() {
        /* prepare */
        SecretValidationResult validationResult = new SecretValidationResult();

        /* execute */
        validationResult.setValidationStatus(null);

        /* test */
        assertEquals(SecretValidationStatus.NO_VALIDATION_CONFIGURED, validationResult.getValidationStatus());

    }

}
