// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SimpleStringUtilsTest {

    @ParameterizedTest
    @CsvSource({ "name", "age", "test" })
    void validNames_hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters_no_additional_is_true(String string) {
        assertThat(SimpleStringUtils.hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters(string), is(true));
    }

    @ParameterizedTest
    @CsvSource({ "name-", "age_", "test-1" })
    void invalidNames_hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters_no_additional_is_false(String string) {
        assertThat(SimpleStringUtils.hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters(string), is(false));
    }

    @ParameterizedTest
    @CsvSource({ "name-", "age_", "test-1" })
    void validNames_hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters_with_additional_is_true(String string) {
        assertThat(SimpleStringUtils.hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters(string, '-', '_'), is(true));
    }

    @ParameterizedTest
    @CsvSource({ "n$me-", "a@e_", "t§st-1" })
    void invalidNames_hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters_with_additional_is_false(String string) {
        assertThat(SimpleStringUtils.hasOnlyAlphabeticDigitOrAdditionalAllowedCharacters(string, '-', '_'), is(false));
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
