// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class ProjectIdValidationImplTest {

    private ProjectIdValidationImpl validationToTest = new ProjectIdValidationImpl();

    private static final String VALID_PROJECT_ID_WITH_255_CHARS = "a0123456789b0123456789c0123456789d0123456789e0123456789f0123456789g0123456789h0123456789i0123456789j0123456789k0123456789l0123456789m0123456789n0123456789o0123456789p0123456789q0123456789r0123456789s0123456789t0123456789u0123456789v0123456789w0123456789yz";

    @ParameterizedTest
    @ValueSource(strings = { "a2", "i-am-with-hyphens", "i_am_with_underscore", VALID_PROJECT_ID_WITH_255_CHARS })
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
            VALID_PROJECT_ID_WITH_255_CHARS + "x" })
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
