// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EmailValidationImplTest {

    private EmailValidationImpl validationToTest;

    @Before
    public void before() {
        validationToTest = new EmailValidationImpl();
    }

    @Test
    public void somebody_at_gmail_address_is_valid() {
        assertTrue(validationToTest.validate("somebody@gmail.com").isValid());
    }

    @Test
    public void mail_with_long_prefix_having_not_only_chars_but_numbers_underscores_dots_and_hyphen_umlauts_and_dollar_is_valid() {
        assertTrue(validationToTest.validate("the_42-is_a_wellknown_Number$äöü.and-also.very_long@example.com").isValid());
    }

    @Test
    public void mail_with_space_is_not_valid() {
        assertFalse(validationToTest.validate("not valid@example.com").isValid());
    }

    @Test
    public void mail_with_colon_is_not_valid() {
        assertFalse(validationToTest.validate("not:valid@example.com").isValid());
    }

    @Test
    public void null_is_invalid() {
        assertFalse(validationToTest.validate((String) null).isValid());
    }

    @Test
    public void empty_is_invalid() {
        assertFalse(validationToTest.validate("").isValid());
    }

}
