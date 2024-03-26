// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class UserInputAssertionTest {

    private UserInputAssertion assertToTest;

    @Before
    public void before() {
        assertToTest = new UserInputAssertion();
        /*
         * we do not add here the mocks but only inside tests, so if another internal
         * validation is used but not tested we got NPE... So test will check
         * automatically all necessary stuff is created and so tested.
         */

    }

    @Test
    public void only_emailvalidation_is_used_when_email_is_asserted() {
        /* prepare */
        String validEmailAddress = "myemail@example.com";

        EmailValidation mockedEmailValidation = mock(EmailValidation.class);
        when(mockedEmailValidation.validate(validEmailAddress)).thenReturn(new ValidationResult());
        assertToTest.emailValidation = mockedEmailValidation;

        /* execute */
        assertToTest.assertIsValidEmailAddress(validEmailAddress);

        /* test */
        verify(mockedEmailValidation).validate(validEmailAddress);
    }

}
