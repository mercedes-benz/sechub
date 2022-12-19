// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.mapping.MappingEntry;

public class MappingEntryValidationImplTest {

    private static final String CONST_80_CHARS = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
    private MappingEntryValidationImpl validationToTest;

    @Before
    public void before() {
        validationToTest = new MappingEntryValidationImpl();
    }

    @Test
    public void all_set_with_valid_content_nothing_wrong() {
        assertTrue(validationToTest.validate(new MappingEntry("something.as.id", "replacement", "comment")).isValid());
        assertTrue(validationToTest.validate(new MappingEntry("", "", "")).isValid());
    }

    @Test
    public void one_null_means_wrong() {
        assertFalse(validationToTest.validate(new MappingEntry(null, "", "")).isValid());
        assertFalse(validationToTest.validate(new MappingEntry("", null, "")).isValid());
        assertFalse(validationToTest.validate(new MappingEntry("", "", null)).isValid());
    }

    @Test
    public void all_null_means_wrong() {
        assertFalse(validationToTest.validate(new MappingEntry(null, null, null)).isValid());
        assertFalse(validationToTest.validate(new MappingEntry()).isValid());
    }

    @Test
    public void check_80_chars_limit_for_id() {
        assertTrue(validationToTest.validate(new MappingEntry(CONST_80_CHARS, "", "")).isValid());
        assertFalse(validationToTest.validate(new MappingEntry(CONST_80_CHARS + "1", "", "")).isValid());
    }

    @Test
    public void check_80_chars_limit_for_replacement() {
        assertTrue(validationToTest.validate(new MappingEntry("", CONST_80_CHARS, "")).isValid());
        assertFalse(validationToTest.validate(new MappingEntry("", CONST_80_CHARS + "1", "")).isValid());
    }

    @Test
    public void check_80_chars_limit_for_comment() {
        assertTrue(validationToTest.validate(new MappingEntry("", "", CONST_80_CHARS)).isValid());
        assertFalse(validationToTest.validate(new MappingEntry("", "", CONST_80_CHARS + "1")).isValid());
    }

}
