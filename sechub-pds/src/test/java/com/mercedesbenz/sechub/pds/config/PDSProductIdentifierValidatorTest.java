// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static org.junit.Assert.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class PDSProductIdentifierValidatorTest {

    private static final String VALID_PRODUCT_ID_WITH_MAX_LENGTH_50 = "12345678901234567890123456789012345678901234567890";
    private PDSProductIdentifierValidator validatorToTest;

    @BeforeEach
    void beforeEach() {
        validatorToTest = new PDSProductIdentifierValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = { "ABCDE_1234_11", "123456789012345678901234567890", VALID_PRODUCT_ID_WITH_MAX_LENGTH_50 })
    void valid_pds_product_identifiers(String productId) {
        /* execute */
        String errorMessage = validatorToTest.createValidationErrorMessage(productId);

        /* test */
        if (errorMessage != null) {
            fail("Expected no error message but got: " + errorMessage);
        }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "$", "x$", "x-", "-", "!", VALID_PRODUCT_ID_WITH_MAX_LENGTH_50 + "x" })
    void invalid_pds_product_identifiers(String productId) {
        /* execute */
        String errorMessage = validatorToTest.createValidationErrorMessage(productId);

        /* test */
        assertNotNull(errorMessage, "Expected an error message but got none!");
    }

}
