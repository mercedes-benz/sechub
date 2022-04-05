// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PDSServerIdentifierValidatorTest {

    private PDSServerIdentifierValidator validatorToTest;

    @Before
    public void before() throws Exception {
        validatorToTest = new PDSServerIdentifierValidator();
    }

    @Test
    public void empty_product_identifiers_return_errormessage() {
        assertNotNull(validatorToTest.createValidationErrorMessage(null));
        assertNotNull(validatorToTest.createValidationErrorMessage(""));
    }

    @Test
    public void product_identifiers_too_long_returns_errormessage() {
        assertNotNull(validatorToTest.createValidationErrorMessage("123456789012345678901234567890x"));
    }

    @Test
    public void product_identifiers_not_too_long_returns_NO_errormessage() {
        assertNull(validatorToTest.createValidationErrorMessage("123456789012345678901234567890"));
        assertNull(validatorToTest.createValidationErrorMessage("ABCDE_1234_11"));
    }

    @Test
    public void product_identifiers_containing_non_alphaebtic_digit_and_no_underscore_are_not_accepted() {
        assertNotNull(validatorToTest.createValidationErrorMessage("$"));
        assertNotNull(validatorToTest.createValidationErrorMessage("x$"));
        assertNotNull(validatorToTest.createValidationErrorMessage("x-"));
        assertNotNull(validatorToTest.createValidationErrorMessage("-"));
        assertNotNull(validatorToTest.createValidationErrorMessage("!"));
    }

}
