// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ApiVersionValidationFactoryTest {

    private ApiVersionValidationFactory factoryToTest;

    @Before
    public void before() throws Exception {
        factoryToTest = new ApiVersionValidationFactory();
    }

    @Test
    public void returns_validation_suitable_for_nothing() {
        /* execute */
        ApiVersionValidation validation = factoryToTest.createValidationAccepting();

        /* test */
        assertFalse(validation.validate("1.0").isValid());
        assertFalse(validation.validate("1.1").isValid());
    }

    @Test
    public void returns_validation_suitable_for_one_version() {
        /* execute */
        ApiVersionValidation validation = factoryToTest.createValidationAccepting("1.1");

        /* test */
        assertTrue(validation.validate("1.1").isValid());

        assertFalse(validation.validate("1.0").isValid());
    }

    @Test
    public void returns_validation_suitable_for_two_versions() {
        /* execute */
        ApiVersionValidation validation = factoryToTest.createValidationAccepting("1.0", "1.1");

        /* test */
        assertTrue(validation.validate("1.0").isValid());
        assertTrue(validation.validate("1.1").isValid());

        assertFalse(validation.validate("0.1").isValid());
    }

}
