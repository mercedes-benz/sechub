// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EmailValidationImplTest {

    private EmailValidationImpl validationToTest;

    private EmailRule emailRule = mock();

    @BeforeEach
    void beforeEach() {
        doNothing().when(emailRule).applyRule(any(), any());
        validationToTest = new EmailValidationImpl(List.of(emailRule));
    }

    @Test
    void somebody_at_gmail_address_is_valid() {
        /* prepare */
        String email = "somebody@gmail.com";

        /* test */
        assertTrue(validationToTest.validate(email).isValid());
        verify(emailRule).applyRule(eq(email), any());
    }

    @Test
    void mail_with_long_prefix_having_not_only_chars_but_numbers_underscores_dots_and_hyphen_umlauts_and_dollar_is_valid() {
        /* prepare */
        String email = "the_42-is_a_wellknown_Number$äöü.and-also.very_long@example.com";

        /* test */
        assertTrue(validationToTest.validate(email).isValid());
        verify(emailRule).applyRule(eq(email), any());
    }

    @Test
    void mail_with_space_is_not_valid() {
        /* prepare */
        String email = "not valid@example.com";

        /* test */
        assertFalse(validationToTest.validate(email).isValid());
        verify(emailRule).applyRule(eq(email), any());
    }

    @Test
    void mail_with_colon_is_not_valid() {
        /* prepare */
        String email = "not:valid@example.com";

        /* test */
        assertFalse(validationToTest.validate(email).isValid());
        verify(emailRule).applyRule(eq(email), any());
    }

    @Test
    void null_is_invalid() {
        /* prepare */
        String email = null;

        /* test */
        assertFalse(validationToTest.validate(email).isValid());
        verify(emailRule, never()).applyRule(eq(email), any());
    }

    @Test
    void empty_is_invalid() {
        /* prepare */
        String email = "";

        /* test */
        assertFalse(validationToTest.validate(email).isValid());
        verify(emailRule).applyRule(eq(email), any());
    }

}
