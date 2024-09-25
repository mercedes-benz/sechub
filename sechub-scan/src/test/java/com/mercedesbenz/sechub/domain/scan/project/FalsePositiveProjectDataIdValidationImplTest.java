// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

class FalsePositiveProjectDataIdValidationImplTest {

    private FalsePositiveProjectDataIdValidationImpl validationToTest;

    @BeforeEach
    void beforeEach() {
        validationToTest = new FalsePositiveProjectDataIdValidationImpl();
    }

    @Test
    void validate_null_returns_validation_result_invalid() {
        /* prepare */
        String target = null;

        /* execute */
        ValidationResult result = validationToTest.validate(target);

        /* test */
        assertFalse(result.isValid());
    }

    @Test
    void validate_empty_string_returns_validation_result_invalid() {
        /* prepare */
        String target = "";

        /* execute */
        ValidationResult result = validationToTest.validate(target);

        /* test */
        assertFalse(result.isValid());
    }

    @Test
    void validate_whitespace_string_returns_validation_result_invalid() {
        /* prepare */
        String target = "           ";

        /* execute */
        ValidationResult result = validationToTest.validate(target);

        /* test */
        assertFalse(result.isValid());
    }

    @Test
    void validate_string_longer_than_100_chars_returns_validation_result_invalid() {
        /* prepare */
        String target = "a".repeat(101);

        /* execute */
        ValidationResult result = validationToTest.validate(target);

        /* test */
        assertFalse(result.isValid());
    }

    @Test
    void validate_string_with_invalid_char_returns_validation_result_invalid() {
        /* prepare */
        String target = "abcd/efgh";

        /* execute */
        ValidationResult result = validationToTest.validate(target);

        /* test */
        assertFalse(result.isValid());
    }

    @Test
    void validate_valid_string_returns_validation_result_valid() {
        /* prepare */
        // use max length 100 chars
        String target = "a".repeat(50) + "-_" + "1".repeat(48);

        /* execute */
        ValidationResult result = validationToTest.validate(target);

        /* test */
        assertTrue(result.isValid());
    }
}
