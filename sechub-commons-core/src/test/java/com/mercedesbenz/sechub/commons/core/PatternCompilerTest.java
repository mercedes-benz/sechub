// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core;

import static org.assertj.core.api.Assertions.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PatternCompilerTest {

    private PatternCompiler compilerToTest;

    @BeforeEach
    void beforeEach() {
        compilerToTest = new PatternCompiler();
    }

    @Test
    void wrong_regular_expresion_throws_pattern_syntax_exception() {
        assertThatThrownBy(() -> compilerToTest.compile("i-am-wrong(")).isInstanceOf(PatternSyntaxException.class);
    }

    @Test
    void null_regular_expresion_throws_npe() {
        assertThatThrownBy(() -> compilerToTest.compile(null)).isInstanceOf(NullPointerException.class).hasMessageContaining("may not be null");
    }

    @ParameterizedTest
    @ValueSource(strings = { "abc", ".*" })
    void correct_regular_expresion_results_in_pattern(String regExp) {
        /* execute */
        Pattern result = compilerToTest.compile(regExp);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo(regExp);
    }

}
