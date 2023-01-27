// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SecHubDataConfigurationTypeListParserTest {

    private SecHubDataConfigurationTypeListParser parserToTest;

    @BeforeEach
    void beforeEach() {
        parserToTest = new SecHubDataConfigurationTypeListParser();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", ",", " ,", " , ", ",,, , ,,", "x", "x,y", "binary,illegal" })
    void illegal_args_will_return_null_as_result(String string) {

        /* execute */
        Set<SecHubDataConfigurationType> parsed = parserToTest.fetchTypesAsSetOrNull(string);

        /* test */
        assertNull(parsed);
    }

    @ParameterizedTest
    @EnumSource(SecHubDataConfigurationType.class)
    void one_type_name_as_uppercased_results_in_set_with_one_entry(SecHubDataConfigurationType type) {

        /* execute */
        Set<SecHubDataConfigurationType> parsed = parserToTest.fetchTypesAsSetOrNull(type.name().toUpperCase());

        /* test */
        assertNotNull(parsed);
        assertEquals(parsed.size(), 1);
        assertEquals(type, parsed.iterator().next());
    }

    @ParameterizedTest
    @EnumSource(SecHubDataConfigurationType.class)
    void one_type_name_as_lowercased_results_in_set_with_one_entry(SecHubDataConfigurationType type) {

        /* execute */
        Set<SecHubDataConfigurationType> parsed = parserToTest.fetchTypesAsSetOrNull(type.name().toLowerCase());

        /* test */
        assertNotNull(parsed);
        assertEquals(parsed.size(), 1);
        assertEquals(type, parsed.iterator().next());
    }

    @ParameterizedTest
    @ValueSource(strings = { "1:binary", "1:BINARY", "3:NONE,BINARY,source", "2:none,source", "2:source,binary", "2:source , BINARY",
            "3:source,binary,NONE,binary" })
    void correct_args_will_return_args_as_set(String data) {
        /* prepare */
        String[] splitted = data.split(":");
        String string = splitted[1];
        int expectedAmount = Integer.parseInt(splitted[0]);

        /* execute */
        Set<SecHubDataConfigurationType> parsed = parserToTest.fetchTypesAsSetOrNull(string);

        /* test */
        assertNotNull(parsed);
        assertEquals(expectedAmount, parsed.size());
    }
}
