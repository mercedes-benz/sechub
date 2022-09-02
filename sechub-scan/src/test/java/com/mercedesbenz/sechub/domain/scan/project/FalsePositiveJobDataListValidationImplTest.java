// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

class FalsePositiveJobDataListValidationImplTest {

    private static ValidationResult validResult = new ValidationResult();
    private static ValidationResult invalidResult = new ValidationResult();

    private static final int MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES = 500;
    private FalsePositiveJobDataListValidationImpl validationToTest;
    private ApiVersionValidation apiVersionValidation;
    private ApiVersionValidationFactory apiVersionValidationFactory;
    private FalsePositiveJobDataValidation falsePositiveJobDataValidation;

    @BeforeAll
    static void beforeAll() {
        invalidResult.addError("error");
        assertTrue(validResult.isValid());
        assertFalse(invalidResult.isValid());
    }

    @BeforeEach
    void beforeEach() {
        apiVersionValidation = mock(ApiVersionValidation.class);

        apiVersionValidationFactory = mock(ApiVersionValidationFactory.class);
        when(apiVersionValidationFactory.createValidationAccepting(any())).thenReturn(apiVersionValidation);

        falsePositiveJobDataValidation = mock(FalsePositiveJobDataValidation.class);

        validationToTest = new FalsePositiveJobDataListValidationImpl();
        validationToTest.apiVersionValidationFactory = apiVersionValidationFactory;
        validationToTest.falsePositiveJobDataValidation = falsePositiveJobDataValidation;

        // call the post construct method like spring boot does - does some necessary
        // intialization
        validationToTest.postConstruct();

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES })
    void list_with_valid_entries_is_accepted_with_this_size(int amountOfFalsePositiveEntries) {
        /* prepare */
        when(falsePositiveJobDataValidation.validate(any())).thenReturn(validResult);

        FalsePositiveJobDataList list = createListWithEntries(amountOfFalsePositiveEntries);

        /* execute + test (no exception ) */
        assertValid(list, validationToTest);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES })
    void list_with_invalid_entries_is_not_accepted_with_this_size(int amountOfFalsePositiveEntries) {
        /* prepare */
        when(falsePositiveJobDataValidation.validate(any())).thenReturn(invalidResult);

        FalsePositiveJobDataList list = createListWithEntries(amountOfFalsePositiveEntries);

        /* execute + test (no exception ) */
        assertThrows(NotAcceptableException.class, () -> assertValid(list, validationToTest));
    }

    @ParameterizedTest
    @ValueSource(ints = { MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES + 1 })
    void list_with_valid_entries_is_NOT_accepted_with_this_size(int amountOfFalsePositiveEntries) {
        /* prepare */
        when(falsePositiveJobDataValidation.validate(any())).thenReturn(validResult);

        FalsePositiveJobDataList list = createListWithEntries(MAXIMUM_ACCEPTED_AMOUNT_OF_ENTRIES + 1);

        /* execute + test (no exception ) */
        assertThrows(NotAcceptableException.class, () -> assertValid(list, validationToTest));
    }

    private FalsePositiveJobDataList createListWithEntries(int amountOfFalsePositiveEntries) {
        FalsePositiveJobDataList list = new FalsePositiveJobDataList();
        for (int i = 0; i < amountOfFalsePositiveEntries; i++) {
            FalsePositiveJobData data = new FalsePositiveJobData();
            list.getJobData().add(data);
        }
        /* internal sanity check for this data */
        assertEquals(amountOfFalsePositiveEntries, list.getJobData().size(), "sanity check failed - test data wrong!");
        return list;
    }

}
