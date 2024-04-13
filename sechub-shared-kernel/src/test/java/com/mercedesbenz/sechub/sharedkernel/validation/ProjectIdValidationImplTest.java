// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class ProjectIdValidationImplTest {

    private ProjectIdValidationImpl validationToTest = new ProjectIdValidationImpl();

    private static final String VALID_PROJECT_ID_WITH_40_CHARS = "a2345678901234567890b2345678901234567890";

    @ParameterizedTest
    @ValueSource(strings = { "a2", "i-am-with-hyphens", "i_am_with_underscore", VALID_PROJECT_ID_WITH_40_CHARS })
    void valid_projectIds(String projectId) {
        /* execute */
        ValidationResult validationResult = validationToTest.validate(projectId);

        /* test */
        if (!validationResult.isValid()) {
            fail("Project id not valid - but should be, validation message:" + validationResult.getErrorDescription());
        }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "a", "i.am.with.dot", "i-am/slashy", "with\\backslash", "percent%", "dollar$", "question?", "colon:", "exclamationmark!",
            VALID_PROJECT_ID_WITH_40_CHARS + "x" })
    void invalid_projectIds(String projectId) {
        /* execute */
        ValidationResult validationResult = validationToTest.validate(projectId);

        /* test */
        if (validationResult.isValid()) {
            fail("Project id valid - but should not be, validation message:" + validationResult.getErrorDescription());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "ALPHA", "Alpha-centauri", "gammA" })
    void invalid_but_not_when_lowercased(String projectId) {
        /* execute */
        ValidationResult validationResult = validationToTest.validate(projectId.toLowerCase());

        /* test */
        // precondition check that this is valid when lower cased...
        assertTrue(validationResult.isValid(), "Project id as lowercased must be valid but isn't");

        // not valid when there are upper cased characters
        validationResult = validationToTest.validate(projectId);
        assertFalse(validationResult.isValid(), "Project id contains uppercase but is valid?");

    }
}
