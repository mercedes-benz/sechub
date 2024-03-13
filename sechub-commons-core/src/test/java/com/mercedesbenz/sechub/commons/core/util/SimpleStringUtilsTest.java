// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class SimpleStringUtilsTest {

    @ValueSource(strings = { "a", "ab", "$*abcdefghiujklmnop" })
    @ParameterizedTest
    void obfuscate_always_obfuscated_when_length_0(String text) {
        assertEquals("*****", SimpleStringUtils.createObfuscatedString(text, 0));
    }

    @Test
    void obfuscate_always_obfuscated_when_length_3_but_only_2_chars() {
        /* prepare */
        String text = "abc";

        /* execute + test */
        assertEquals("ab*****", SimpleStringUtils.createObfuscatedString(text, 2));
    }

    @Test
    void obfuscate_always_obfuscated_when_length_2_but_only_3_chars() {
        /* prepare */
        String text = "ab";

        /* execute + test */
        assertEquals("ab*****", SimpleStringUtils.createObfuscatedString(text, 3));
    }

    @Test
    void do_not_obfuscate_when_length_smaller_null() {
        /* prepare */
        String text = "ab23zr9hfiedlshfl";

        /* execute + test */
        assertEquals("ab23zr9hfiedlshfl", SimpleStringUtils.createObfuscatedString(text, -2));
    }

    @Test
    void return_null_when_string_parameter_null() {
        /* prepare */
        String text = null;

        /* execute + test */
        assertNull(SimpleStringUtils.createObfuscatedString(text, 0));
    }

    @ValueSource(strings = { "abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZ" })
    @ParameterizedTest
    void isLatinLetter_valid_values(String value) {
        for (char c : value.toCharArray()) {
            assertTrue(SimpleStringUtils.isStandardAsciiLetter(c));
        }
    }

    @Test
    void isLatinLetter_valid_values_use_ordinal_numbers_to_check() {
        for (int i = 65; i <= 90; i++) {
            assertTrue(SimpleStringUtils.isStandardAsciiLetter((char) i), "Char with:" + i + " = " + ((char) i));
        }
        for (int i = 97; i <= 122; i++) {
            assertTrue(SimpleStringUtils.isStandardAsciiLetter((char) i));
        }
    }

    @Test
    void isLatinLetter_invvalid_values_use_ordinal_numbers_to_check() {
        for (int i = 0; i <= 64; i++) {
            assertFalse(SimpleStringUtils.isStandardAsciiLetter((char) i), "Char with:" + i + " = " + ((char) i));
        }
        for (int i = 91; i <= 96; i++) {
            assertFalse(SimpleStringUtils.isStandardAsciiLetter((char) i), "Char with:" + i + " = " + ((char) i));
        }
        for (int i = 123; i <= 65535; i++) {
            assertFalse(SimpleStringUtils.isStandardAsciiLetter((char) i), "Char with:" + i + " = " + ((char) i));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 200, 300, 400, -1, -20 })
    void stringsContainingValidIntegersCanBeConvertedToIntWithoutDefault(int value) {
        /* prepare */
        String data = String.valueOf(value);

        /* execute */
        int result = SimpleStringUtils.toIntOrDefault(data, 4711);

        /* test */
        assertEquals(value, result);
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = { "1.2", "$" })
    void stringsNotContainingValidIntegersWillBeConvertedToIntWithDefaultInstead(String value) {
        /* execute */
        int result = SimpleStringUtils.toIntOrDefault(value, 4711);

        /* test */
        assertEquals(4711, result);
    }

    @ParameterizedTest
    @CsvSource({ "*.go|/**/subfolder/**", "age", "test" })
    void createListForCommaSeparatedValues(String testDataValuesSeparatedByPipe) {
        /* prepare */
        String[] valuesArray = testDataValuesSeparatedByPipe.split("\\|");

        String valuesCommaSeparated = String.join(",", valuesArray);
        List<String> expectedValues = Arrays.asList(valuesArray);

        /* execute */
        List<String> createdValues = SimpleStringUtils.createListForCommaSeparatedValues(valuesCommaSeparated);

        /* execute + test */
        assertEquals(expectedValues, createdValues);

    }

    @Test
    void createListForCommaSeparatedValues_spaces_in_data_have_no_influence() {
        /* prepare */
        List<String> expectedElements = new ArrayList<>();
        expectedElements.add("*.go");
        expectedElements.add("/**/subfolder/**");
        expectedElements.add("something with spaces");

        /* execute + test */
        assertEquals(expectedElements, SimpleStringUtils.createListForCommaSeparatedValues("*.go,/**/subfolder/**,something with spaces"));
        assertEquals(expectedElements, SimpleStringUtils.createListForCommaSeparatedValues("*.go, /**/subfolder/**,something with spaces "));
        assertEquals(expectedElements, SimpleStringUtils.createListForCommaSeparatedValues("  *.go,    /**/subfolder/**  ,something with spaces  "));
        assertEquals(expectedElements, SimpleStringUtils.createListForCommaSeparatedValues("  *.go,    /**/subfolder/**  ,  something with spaces"));

    }

    @Test
    void createListForCommaSeparatedValues_null_results_in_empty_list() {

        /* execute */
        List<String> result = SimpleStringUtils.createListForCommaSeparatedValues(null);

        /* test */
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createListForCommaSeparatedValues_only_spaces_results_in_empty_list() {
        /* execute + test */
        assertTrue(SimpleStringUtils.createListForCommaSeparatedValues("").isEmpty());
        assertTrue(SimpleStringUtils.createListForCommaSeparatedValues(" ").isEmpty());
        assertTrue(SimpleStringUtils.createListForCommaSeparatedValues("  ").isEmpty());
        assertTrue(SimpleStringUtils.createListForCommaSeparatedValues(", , , ,").isEmpty());
        assertTrue(SimpleStringUtils.createListForCommaSeparatedValues(",    , , ,").isEmpty());
    }

    @ParameterizedTest
    @CsvSource({ "name", "age", "test", "UPPERCASED" })
    void validNames_hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters_no_additional_is_true(String string) {
        assertThat(SimpleStringUtils.hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(string), is(true));
    }

    @ParameterizedTest
    @CsvSource({ "name-", "age_", "test-1", "äpfel", "🦊", "Señor" })
    void invalidNames_hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters_no_additional_is_false(String string) {
        assertThat(SimpleStringUtils.hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(string), is(false));
    }

    @ParameterizedTest
    @CsvSource({ "name-", "age_", "test-1" })
    void validNames_hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters_with_additional_is_true(String string) {
        assertThat(SimpleStringUtils.hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(string, '-', '_'), is(true));
    }

    @ParameterizedTest
    @CsvSource({ "n$me-", "a@e_", "t§st-1" })
    void invalidNames_hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters_with_additional_is_false(String string) {
        assertThat(SimpleStringUtils.hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(string, '-', '_'), is(false));
    }

    @Test
    void isTrimmedEquals_null_null_is_true() {
        assertThat(SimpleStringUtils.isTrimmedEqual(null, null), is(true));
    }

    @Test
    void isTrimmedEquals_empty_null_is_true() {
        assertThat(SimpleStringUtils.isTrimmedEqual("", null), is(true));
    }

    @Test
    void isTrimmedEquals_null_empty_is_true() {
        assertThat(SimpleStringUtils.isTrimmedEqual(null, ""), is(true));
    }

    @Test
    void isTrimmedEquals_empty_empty_is_true() {
        assertThat(SimpleStringUtils.isTrimmedEqual("", ""), is(true));
        assertThat(SimpleStringUtils.isTrimmedEqual("", "  "), is(true));
        assertThat(SimpleStringUtils.isTrimmedEqual(" ", "  "), is(true));
    }

    @Test
    void isTrimmedEquals_different_strings_is_false() {
        assertThat(SimpleStringUtils.isTrimmedEqual("a", "b"), is(false));
        assertThat(SimpleStringUtils.isTrimmedEqual("alpha", "beta"), is(false));
    }

    @Test
    void isTrimmedEquals_similar_strings_is_true() {
        assertThat(SimpleStringUtils.isTrimmedEqual("alpha", "   alpha"), is(true));
        assertThat(SimpleStringUtils.isTrimmedEqual("alpha ", "   alpha"), is(true));
        assertThat(SimpleStringUtils.isTrimmedEqual("   alpha ", "   alpha"), is(true));
        assertThat(SimpleStringUtils.isTrimmedEqual("   alpha ", "   \t\talpha\n"), is(true));
    }

    @Test
    void null_equals_null_returns_true() {
        assertThat(SimpleStringUtils.equals(null, null), is(true));
    }

    @Test
    void null_equals_not_empty_strings_returns_false() {
        assertThat(SimpleStringUtils.equals(null, "string"), is(false));
        assertThat(SimpleStringUtils.equals(null, ""), is(false));
        assertThat(SimpleStringUtils.equals("", null), is(false));
        assertThat(SimpleStringUtils.equals("string", null), is(false));
    }

    @Test
    void string1_equals_string2_returns_false() {
        assertThat(SimpleStringUtils.equals("string1", "string2"), is(false));
    }

    @Test
    void same_strings_equals_returns_true() {
        assertThat(SimpleStringUtils.equals("string1", "string1"), is(true));
        assertThat(SimpleStringUtils.equals("", ""), is(true));
    }

    @Test
    void string1_is_part_of_string2_returns_false() {
        assertThat(SimpleStringUtils.startsWith("string1", "string2"), is(false));
        assertThat(SimpleStringUtils.startsWith("string2", "string1"), is(false));
    }

    @Test
    void string1_is_part_of_string1a_returns_true() {
        assertThat(SimpleStringUtils.startsWith("string1", "string1a"), is(true));
    }

    @Test
    void string1a_is_part_of_string1_returns_false() {
        assertThat(SimpleStringUtils.startsWith("string1a", "string1"), is(false));
    }

    @Test
    void string1_is_part_of_string1_returns_true() {
        assertThat(SimpleStringUtils.startsWith("string1", "string1a"), is(true));
    }

}
