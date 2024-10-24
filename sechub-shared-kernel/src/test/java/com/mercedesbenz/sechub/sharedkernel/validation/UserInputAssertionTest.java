// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserInputAssertionTest {

    private UserInputAssertion assertToTest;

    @BeforeEach
    void beforeEach() {
        assertToTest = new UserInputAssertion();
        /*
         * we do not add here the mocks but only inside tests, so if another internal
         * validation is used but not tested we got NPE... So test will check
         * automatically all necessary stuff is created and so tested.
         */

    }

    @Test
    void only_emailvalidation_is_used_when_email_is_asserted() {
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

    @Test
    void templateIdValidation_used_for_assert_templateId() {

        /* prepar */
        TemplateIdValidation templateIdValidation = mock(TemplateIdValidation.class);
        when(templateIdValidation.validate("x")).thenReturn(new ValidationResult());
        assertToTest.templateIdValidation = templateIdValidation;

        /* execute */
        assertToTest.assertIsValidTemplateId("x");

        /* test */
        verify(templateIdValidation).validate("x");
    }

    @Test
    void assetIdValidation_used_for_assert_assetId() {

        /* prepar */
        AssetIdValidation assetIdValidation = mock(AssetIdValidation.class);
        when(assetIdValidation.validate("x")).thenReturn(new ValidationResult());
        assertToTest.assetIdValidation = assetIdValidation;

        /* execute */
        assertToTest.assertIsValidAssetId("x");

        /* test */
        verify(assetIdValidation).validate("x");
    }

}
