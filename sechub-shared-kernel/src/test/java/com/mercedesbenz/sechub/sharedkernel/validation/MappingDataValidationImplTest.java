// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;

public class MappingDataValidationImplTest {

    private MappingDataValidationImpl validationToTest;
    private MappingEntryValidation mappingEntryValidation;
    private MappingEntry entry1;
    private MappingEntry entry2;

    @Before
    public void before() {

        validationToTest = new MappingDataValidationImpl();

        mappingEntryValidation = mock(MappingEntryValidation.class);
        entry1 = new MappingEntry("pattern1", "replace1", "comment1");
        entry2 = new MappingEntry("pattern2", "replace2", "comment2");

        validationToTest.mappingEntryValidation = mappingEntryValidation;

    }

    @Test
    public void having_2_mapping_entries_calls_map_entry_validation_two_times() {
        /* prepare */
        MappingData data = new MappingData();
        data.getEntries().add(entry1);
        data.getEntries().add(entry2);

        when(mappingEntryValidation.validate(any())).thenReturn(new ValidationResult());

        /* execute */
        validationToTest.validate(data);

        /* test */
        verify(mappingEntryValidation).validate(entry1);
        verify(mappingEntryValidation).validate(entry2);
    }

    @Test
    public void having_2_mapping_entries_calls_and_two_good_than_valid() {
        /* prepare */
        MappingData data = new MappingData();
        data.getEntries().add(entry1);
        data.getEntries().add(entry2);

        when(mappingEntryValidation.validate(any())).thenReturn(new ValidationResult());

        /* execute +test */
        assertTrue(validationToTest.validate(data).isValid());

    }

    @Test
    public void having_2_mapping_entries_calls_and_none_good_than_invalid() {
        /* prepare */
        MappingData data = new MappingData();
        data.getEntries().add(entry1);
        data.getEntries().add(entry2);

        ValidationResult result1 = new ValidationResult();
        result1.addError("error1");
        ValidationResult result2 = new ValidationResult();
        result1.addError("error2");

        when(mappingEntryValidation.validate(entry1)).thenReturn(result1);
        when(mappingEntryValidation.validate(entry2)).thenReturn(result2);

        /* execute +test */
        assertFalse(validationToTest.validate(data).isValid());
    }

    @Test
    public void having_2_mapping_entries_calls_and_entry1_bad_than_invalid() {
        /* prepare */
        MappingData data = new MappingData();
        data.getEntries().add(entry1);
        data.getEntries().add(entry2);

        ValidationResult result1 = new ValidationResult();
        result1.addError("error1");
        ValidationResult result2 = new ValidationResult();

        when(mappingEntryValidation.validate(entry1)).thenReturn(result1);
        when(mappingEntryValidation.validate(entry2)).thenReturn(result2);

        /* execute */
        boolean valid = validationToTest.validate(data).isValid();

        /* test */
        assertFalse(valid);

    }

    @Test
    public void having_2_mapping_entries_calls_and_entry2_bad_than_invalid() {
        /* prepare */
        MappingData data = new MappingData();
        data.getEntries().add(entry1);
        data.getEntries().add(entry2);

        ValidationResult result1 = new ValidationResult();
        ValidationResult result2 = new ValidationResult();
        result2.addError("error1");

        when(mappingEntryValidation.validate(entry1)).thenReturn(result1);
        when(mappingEntryValidation.validate(entry2)).thenReturn(result2);

        /* execute +test */
        assertFalse(validationToTest.validate(data).isValid());

    }

}
