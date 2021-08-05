package com.daimler.sechub.sharedkernel.project;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ProjectAccessLevelConverterTest {

    private ProjectAccessLevelJPAConverter converterToTest;

    @BeforeEach
    void beforeEach() {
        converterToTest = new ProjectAccessLevelJPAConverter();
    }

    @ParameterizedTest
    @CsvSource({ "FULL,full", "READ_ONLY,read_only", "NO_ACCESS,no_access" })
    void ids_results_in_expected_access_levels_by_name(String value, String id) {
        /* prepare */
        ProjectAccessLevel expectedLevel = ProjectAccessLevel.valueOf(value);

        /* execute */
        ProjectAccessLevel result = converterToTest.convertToEntityAttribute(id);

        /* test */
        assertEquals(expectedLevel, result);

    }
    
    @Test
    void null_id_results_in_null_access_level() {
        /* prepare */
        ProjectAccessLevel expectedLevel = null;

        /* execute */
        ProjectAccessLevel result = converterToTest.convertToEntityAttribute(null);

        /* test */
        assertEquals(expectedLevel, result);

    }
    
    @Test
    void unknown_id_results_in_null_access_level() {
        /* prepare */
        ProjectAccessLevel expectedLevel = null;

        /* execute */
        ProjectAccessLevel result = converterToTest.convertToEntityAttribute("i am an unknown string");

        /* test */
        assertEquals(expectedLevel, result);

    }

}
