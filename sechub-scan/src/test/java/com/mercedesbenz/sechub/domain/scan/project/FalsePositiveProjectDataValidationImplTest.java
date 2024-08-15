// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

class FalsePositiveProjectDataValidationImplTest {

    private FalsePositiveProjectDataValidationImpl validationToTest;

    private static final FalsePositiveProjectDataIdValidation idValidation = mock();
    private static final WebscanFalsePositiveProjectDataValidation webscanValidation = mock();

    @SuppressWarnings("unchecked")
    @BeforeEach
    void beforeEach() {
        Mockito.reset(idValidation, webscanValidation);

        validationToTest = new FalsePositiveProjectDataValidationImpl(idValidation, webscanValidation);

        when(idValidation.validate(any())).thenReturn(new ValidationResult());
        when(webscanValidation.validate(any())).thenReturn(new ValidationResult());
    }

    @Test
    void no_project_data_validation_returns_invalid_result() {
        /* prepare */
        FalsePositiveProjectData projectData = null;

        /* execute */
        ValidationResult result = validationToTest.validate(projectData);

        /* test */
        assertFalse(result.isValid());
    }

    @Test
    void without_optional_parts_returns_valid_result() {
        /* prepare */
        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        projectData.setComment("a".repeat(501));

        /* execute */
        ValidationResult result = validationToTest.validate(projectData);

        /* test */
        assertFalse(result.isValid());
    }

}
