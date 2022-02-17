// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MappingIdValidationImplTest {

    private static final String CONST_80_CHARS = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
    private MappingIdValidationImpl validationToTest;

    @Before
    public void before() {
        validationToTest = new MappingIdValidationImpl();
    }

    @Test
    public void check_5_chars_min_for_id() {
        assertTrue(validationToTest.validate("12345").isValid());
        assertFalse(validationToTest.validate("1234").isValid());
        assertFalse(validationToTest.validate("123").isValid());
        assertFalse(validationToTest.validate("").isValid());
    }

    @Test
    public void check_80_chars_max_for_id() {
        assertTrue(validationToTest.validate(CONST_80_CHARS).isValid());
        assertFalse(validationToTest.validate(CONST_80_CHARS + "1").isValid());
    }

    @Test
    public void check_whitespaces_not_accepted_inside_id() {
        assertFalse(validationToTest.validate("12345 6").isValid());
        assertFalse(validationToTest.validate(" 123456").isValid());
        assertFalse(validationToTest.validate("123456 ").isValid());

        assertFalse(validationToTest.validate("123456\n").isValid());
        assertFalse(validationToTest.validate("123456\r").isValid());
    }

    @Test
    public void check_percentage_not_accepted_inside_id() {
        assertFalse(validationToTest.validate("12345%6").isValid());
    }

}
