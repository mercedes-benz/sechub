// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class UserIdValidationImplTest {

    private static final String VALID_USER_ID_WITH_40_CHARS = "a2345678901234567890b2345678901234567890";
    private UserIdValidationImpl userIdValidation = new UserIdValidationImpl();

    @ParameterizedTest
    @ValueSource(strings = { "a2345", "i-am-with-hyphens", "i_am_with_underscore", VALID_USER_ID_WITH_40_CHARS })
    void valid_userIds(String userId) {
        /* execute */
        ValidationResult userIdValidationResult = userIdValidation.validate(userId);

        /* test */
        if (!userIdValidationResult.isValid()) {
            fail("User id not valid - but should be, validation message:" + userIdValidationResult.getErrorDescription());
        }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "a", "a234", "i.am.with.dot", "i-am/slashy", "with\\backslash", "percent%", "dollar$", "question?", "colon:", "exclamationmark!",
            VALID_USER_ID_WITH_40_CHARS + "x" })
    void invalid_userIds(String userId) {
        /* execute */
        ValidationResult userIdValidationResult = userIdValidation.validate(userId);

        /* test */
        if (userIdValidationResult.isValid()) {
            fail("User id valid - but should not be, validation message:" + userIdValidationResult.getErrorDescription());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "ALPHA", "Alpha-centauri", "gammA" })
    void invalid_but_not_when_lowercased(String userId) {
        /* execute */
        ValidationResult userIdValidationResult = userIdValidation.validate(userId.toLowerCase());

        /* test */
        // precondition check that this is valid when lower cased...
        assertTrue(userIdValidationResult.isValid(), "User id as lowercased must be valid but isn't");

        // not valid when there are upper cased characters
        userIdValidationResult = userIdValidation.validate(userId);
        assertFalse(userIdValidationResult.isValid(), "User id contains uppercase but is valid?");

    }

}
