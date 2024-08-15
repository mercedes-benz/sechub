// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation.assertValid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

class FalsePositiveDataListValidationImplTest {

    private static ValidationResult validResult = new ValidationResult();
    private static ValidationResult invalidResult = new ValidationResult();

    private static final int MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES = 500;
    private FalsePositiveDataListValidationImpl validationToTest;
    private static final ApiVersionValidation apiVersionValidation = mock();
    private static final ApiVersionValidationFactory apiVersionValidationFactory = mock();
    private static final FalsePositiveJobDataValidation falsePositiveJobDataValidation = mock();
    private static final FalsePositiveProjectDataValidation falsePositiveProjectDataValidation = mock();

    @BeforeAll
    static void beforeAll() {
        invalidResult.addError("error");
        assertTrue(validResult.isValid());
        assertFalse(invalidResult.isValid());
    }

    @BeforeEach
    void beforeEach() {
        /* @formatter:off */
        Mockito.reset(apiVersionValidation,
                apiVersionValidationFactory,
                falsePositiveJobDataValidation,
                falsePositiveProjectDataValidation);
        /* @formatter:on */

        when(apiVersionValidationFactory.createValidationAccepting(any())).thenReturn(apiVersionValidation);

        /* @formatter:off */
        validationToTest = new FalsePositiveDataListValidationImpl(apiVersionValidationFactory,
                falsePositiveJobDataValidation,
                falsePositiveProjectDataValidation);
        /* @formatter:on */
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES })
    void job_data_list_with_valid_entries_is_accepted_with_this_size(int amountOfFalsePositiveEntries) {
        /* prepare */
        when(falsePositiveJobDataValidation.validate(any())).thenReturn(validResult);

        FalsePositiveDataList list = createJobDataListWithEntries(amountOfFalsePositiveEntries);

        /* execute + test (no exception ) */
        assertValid(list, validationToTest);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES })
    void job_data_list_with_invalid_entries_is_not_accepted_with_this_size(int amountOfFalsePositiveEntries) {
        /* prepare */
        when(falsePositiveJobDataValidation.validate(any())).thenReturn(invalidResult);

        FalsePositiveDataList list = createJobDataListWithEntries(amountOfFalsePositiveEntries);

        /* execute + test (no exception ) */
        assertThrows(NotAcceptableException.class, () -> assertValid(list, validationToTest));
    }

    @ParameterizedTest
    @ValueSource(ints = { MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES + 1 })
    void job_data_list_with_valid_entries_is_NOT_accepted_with_this_size(int amountOfFalsePositiveEntries) {
        /* prepare */
        when(falsePositiveJobDataValidation.validate(any())).thenReturn(validResult);

        FalsePositiveDataList list = createJobDataListWithEntries(MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES + 1);

        /* execute + test (no exception ) */
        assertThrows(NotAcceptableException.class, () -> assertValid(list, validationToTest));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES })
    void project_data_list_with_valid_entries_is_accepted_with_this_size(int amountOfFalsePositiveEntries) {
        /* prepare */
        when(falsePositiveProjectDataValidation.validate(any())).thenReturn(validResult);

        FalsePositiveDataList list = createProjectDataListWithEntries(amountOfFalsePositiveEntries);

        /* execute + test (no exception ) */
        assertValid(list, validationToTest);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES })
    void project_data_list_with_invalid_entries_is_not_accepted_with_this_size(int amountOfFalsePositiveEntries) {
        /* prepare */
        when(falsePositiveProjectDataValidation.validate(any())).thenReturn(invalidResult);

        FalsePositiveDataList list = createProjectDataListWithEntries(amountOfFalsePositiveEntries);

        /* execute + test (no exception ) */
        assertThrows(NotAcceptableException.class, () -> assertValid(list, validationToTest));
    }

    @ParameterizedTest
    @ValueSource(ints = { MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES + 1 })
    void project_data_list_with_valid_entries_is_NOT_accepted_with_this_size(int amountOfFalsePositiveEntries) {
        /* prepare */
        when(falsePositiveProjectDataValidation.validate(any())).thenReturn(validResult);

        FalsePositiveDataList list = createProjectDataListWithEntries(MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES + 1);

        /* execute + test (no exception ) */
        assertThrows(NotAcceptableException.class, () -> assertValid(list, validationToTest));
    }

    @Test
    void job_data_list_mixed_with_project_data_list_with_valid_entries_is_NOT_accepted_with_this_size() {
        /* prepare */
        when(falsePositiveProjectDataValidation.validate(any())).thenReturn(validResult);
        when(falsePositiveJobDataValidation.validate(any())).thenReturn(validResult);

        FalsePositiveDataList list = createJobDataListWithEntries(MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES);
        list.getProjectData().add(new FalsePositiveProjectData());

        /* execute + test (no exception ) */
        assertThrows(NotAcceptableException.class, () -> assertValid(list, validationToTest));
    }

    private FalsePositiveDataList createJobDataListWithEntries(int amountOfFalsePositiveEntries) {
        FalsePositiveDataList list = new FalsePositiveDataList();
        for (int i = 0; i < amountOfFalsePositiveEntries; i++) {
            FalsePositiveJobData data = new FalsePositiveJobData();
            list.getJobData().add(data);
        }
        /* internal sanity check for this data */
        assertEquals(amountOfFalsePositiveEntries, list.getJobData().size(), "sanity check failed - test data wrong!");
        return list;
    }

    private FalsePositiveDataList createProjectDataListWithEntries(int amountOfFalsePositiveEntries) {
        FalsePositiveDataList list = new FalsePositiveDataList();
        for (int i = 0; i < amountOfFalsePositiveEntries; i++) {
            FalsePositiveProjectData data = new FalsePositiveProjectData();
            list.getProjectData().add(data);
        }
        /* internal sanity check for this data */
        assertEquals(amountOfFalsePositiveEntries, list.getProjectData().size(), "sanity check failed - test data wrong!");
        return list;
    }

}
