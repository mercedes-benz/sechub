// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class SystemEnvironmentVariableSupportTest {

    private SystemEnvironment systemEnvironment;
    private SystemEnvironmentVariableSupport supportToTest;

    @BeforeEach
    void beforeEach() {
        systemEnvironment = mock(SystemEnvironment.class);

        supportToTest = new SystemEnvironmentVariableSupport(systemEnvironment);
    }

    @ParameterizedTest
    @ValueSource(strings = { "VALUE", "value", "v" })
    @NullSource
    @EmptySource
    void isValueLikeEnvironmentVariableValue_returns_true_when_same_value_in_environment_value(String value) {

        /* prepare */
        String envVariableName = "SOME_VARIABLE";

        when(systemEnvironment.getEnv(envVariableName)).thenReturn(value);

        /* execute */
        boolean result = supportToTest.isValueLikeEnvironmentVariableValue(envVariableName, value);

        /* test */
        assertTrue(result);

    }

    @ParameterizedTest
    @ValueSource(strings = { "", "some_variable", "test" })
    @NullSource
    @EmptySource
    void isValueLikeEnvironmentVariableValue_returns_false_when_not_same_value_in_environment_value(String value) {

        /* prepare */
        String envVariableName = "SOME_VARIABLE";

        when(systemEnvironment.getEnv(envVariableName)).thenReturn(value + "-other");

        /* execute */
        boolean result = supportToTest.isValueLikeEnvironmentVariableValue(envVariableName, value);

        /* test */
        assertFalse(result);

    }

    @ParameterizedTest
    @ValueSource(strings = { "a", "username", "env_no_colon", "ENV_NO_COLON_UPPERCASED" })
    public void when_key_has_no_prefix_the_origin_value_is_returned(String variableName) {
        assertEquals(variableName, supportToTest.getValueOrVariableContent(variableName));
        verify(systemEnvironment, never()).getEnv(any()); // system environment is never inspected
    }

    /* @formatter:off */
    @ParameterizedTest
    @CsvSource({
        "ENV:A_TEST_VARIABLE,A_TEST_VARIABLE",
        "env:small_variable,small_variable",
        "env:BIG_VARIABLE,BIG_VARIABLE",
        "env: VAR1_SPACE_BEFORE_IS_TRIMMED,VAR1_SPACE_BEFORE_IS_TRIMMED",
        "env:a,a" })
    /* @formatter:on */
    public void when_key_has_prefix_and_system_environment_has_variable_the_content_of_variable_is_resolved(String envVariableData,
            String expectedVariableName) {

        /* prepare */
        String variableContent = "test-content-" + System.currentTimeMillis();

        when(systemEnvironment.getEnv(expectedVariableName)).thenReturn(variableContent);

        /* execute + test */
        assertEquals(variableContent, supportToTest.getValueOrVariableContent(envVariableData));
    }

    @ParameterizedTest
    @CsvSource({ "ENV:A_TEST_VARIABLE,A_TEST_VARIABLE" })
    public void when_key_has_prefix_and_system_environment_has_NOT_variable_null_is_resolved(String envVariableData, String expectedVariableName) {

        /* prepare */
        when(systemEnvironment.getEnv(expectedVariableName)).thenReturn(null);

        /* execute + test */
        assertEquals(null, supportToTest.getValueOrVariableContent(envVariableData));
        verify(systemEnvironment).getEnv(expectedVariableName);

    }

    @ParameterizedTest
    @CsvSource({ "ENV:,", "env:,", "env:   ," })
    public void when_key_has_prefix_but_no_variable_name_null_is_resolved(String envVariableData, String expectedVariableName) {

        /* execute + test */
        assertEquals(null, supportToTest.getValueOrVariableContent(envVariableData));
        verify(systemEnvironment, never()).getEnv(expectedVariableName);

    }

}
