// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.template;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.systemtest.runtime.variable.EnvironmentProvider;

class SystemTestTemplateEngineTest {

    private SystemTestTemplateEngine engineToTest;

    @BeforeEach
    void beforeEach() {
        engineToTest = new SystemTestTemplateEngine();
    }

    @ParameterizedTest
    @ValueSource(strings = { "John Doe", "!Something$$$$" })
    @EmptySource
    @NullSource
    void replaceEnvironmentVariablesWithValues_simple_text_with_one_env_variable(String replacement) {
        /* prepare */
        String content = "This is ${env.USER_NAME}";

        EnvironmentProvider envProvider = mock(EnvironmentProvider.class);
        when(envProvider.getEnv("USER_NAME")).thenReturn(replacement);

        /* execute */
        String result = engineToTest.replaceEnvironmentVariablesWithValues(content, envProvider);

        /* test */
        if (replacement == null) {
            assertEquals("This is ", result); // null is converted to an empty string
        } else {
            assertEquals("This is " + replacement, result);
        }

    }

    @Test
    void replaceEnvironmentVariablesWithValues_simple_text_with_two_env_variable() {
        /* prepare */
        String content = "This is ${env.USER_NAME} and this is his ${env.USER_PWD}";

        EnvironmentProvider envProvider = mock(EnvironmentProvider.class);
        when(envProvider.getEnv("USER_NAME")).thenReturn("u1");
        when(envProvider.getEnv("USER_PWD")).thenReturn("tpwd1");

        /* execute */
        String result = engineToTest.replaceEnvironmentVariablesWithValues(content, envProvider);

        /* test */
        assertEquals("This is u1 and this is his tpwd1", result);

    }

    @ParameterizedTest
    @ValueSource(strings = { "John Doe", "!Something$$$$", "{\"iAmNotEscaped\":\true}" })
    @EmptySource
    void replaceEnvironmentVariablesWithValues_json_with_same_two_env_variables(String replacement) {
        /* prepare */
        String content = "{ \"message\" :\"This is ${env.USER_NAME}\", \"userName\" : \"${env.USER_NAME}\"}";

        EnvironmentProvider envProvider = mock(EnvironmentProvider.class);
        when(envProvider.getEnv("USER_NAME")).thenReturn(replacement);

        /* execute */
        String result = engineToTest.replaceEnvironmentVariablesWithValues(content, envProvider);

        /* test */
        assertEquals("{ \"message\" :\"This is " + replacement + "\", \"userName\" : \"" + replacement + "\"}", result);

    }

    @Test
    void replaceEnvironmentVariablesWithValues_json_replacement_null_is_handled_as_empty_string() {
        /* prepare */
        String content = "{ \"message\" :\"This is ${env.USER_NAME}\", \"userName\" : \"${env.USER_NAME}\"}";

        EnvironmentProvider envProvider = mock(EnvironmentProvider.class);
        when(envProvider.getEnv("USER_NAME")).thenReturn(null);

        /* execute */
        String result = engineToTest.replaceEnvironmentVariablesWithValues(content, envProvider);

        /* test */
        assertEquals("{ \"message\" :\"This is \", \"userName\" : \"\"}", result);
    }

    @Test
    void parse_variable() {
        /* prepare */
        String content = "This is ${env.USER_NAME}";

        /* execute */
        List<TemplateVariableBlock> vars = engineToTest.parseVariableBlocks(content);

        /* test */
        assertNotNull(vars);
        assertEquals(1, vars.size());
        TemplateVariableBlock var1 = vars.get(0);
        assertEquals("${env.USER_NAME}", var1.getComplete());
        assertEquals("env.USER_NAME", var1.getName());

        assertEquals("${env.USER_NAME}", content.subSequence(var1.getStartIndex(), var1.getEndIndex()));

    }

    @Test
    void parse_variable_json_example() {
        /* prepare */
        String content = "{\"variables\":{\"var1\":\"value1\"},\"setup\":{\"comment\":\"This is a comment - even this is replaceable - because we just change the complete JSON... var1=${variables.var1}\"},\"tests\":[]}";

        /* execute */
        List<TemplateVariableBlock> variables = engineToTest.parseVariableBlocks(content);

        /* test */
        assertEquals(1, variables.size());
        TemplateVariableBlock variable = variables.get(0);
        assertEquals("variables.var1", variable.getName());

    }

    @Test
    void parse_variable_first_closed_but_not_second() {
        /* prepare */
        String content = "This is ${env.USER_NAME} and ${env.USER_PWD";

        /* execute */
        List<TemplateVariableBlock> vars = engineToTest.parseVariableBlocks(content);

        /* test */
        assertNotNull(vars);
        assertEquals(1, vars.size());
        TemplateVariableBlock var1 = vars.get(0);
        assertEquals("${env.USER_NAME}", var1.getComplete());
        assertEquals("env.USER_NAME", var1.getName());

        assertEquals("${env.USER_NAME}", content.subSequence(var1.getStartIndex(), var1.getEndIndex()));

    }

    @Test
    void parse_variable_first_not_closed_but_second() {
        /* prepare */
        String content = "This is ${env.USER_NAME and ${env.USER_PWD}";

        /* execute */
        List<TemplateVariableBlock> vars = engineToTest.parseVariableBlocks(content);

        /* test */
        assertNotNull(vars);
        assertEquals(1, vars.size());
        TemplateVariableBlock var1 = vars.get(0);
        assertEquals("${env.USER_NAME and ${env.USER_PWD}", var1.getComplete());
        // in name, white spaces are removed:
        assertEquals("env.USER_NAMEand${env.USER_PWD", var1.getName());

        assertEquals("${env.USER_NAME and ${env.USER_PWD}", content.subSequence(var1.getStartIndex(), var1.getEndIndex()));

    }

    @Test
    void parse_variable_not_closed_returns_in_no_variables() {
        /* prepare */
        String content = "This is ${env.USER_NAME";

        /* execute */
        List<TemplateVariableBlock> vars = engineToTest.parseVariableBlocks(content);

        /* test */
        assertNotNull(vars);
        assertEquals(0, vars.size());
    }

    @Test
    void parse_variables_with_two_variables_one_with_additional_spaces() {
        /* prepare */
        String content = "This is ${env.USER_NAME} and a ${  variables.var1 }";

        /* execute */
        List<TemplateVariableBlock> vars = engineToTest.parseVariableBlocks(content);

        /* test */
        assertNotNull(vars);
        assertEquals(2, vars.size());
        TemplateVariableBlock var1 = vars.get(0);
        assertEquals("${env.USER_NAME}", var1.getComplete());
        assertEquals("env.USER_NAME", var1.getName());

        assertEquals("${env.USER_NAME}", content.subSequence(var1.getStartIndex(), var1.getEndIndex()));

        TemplateVariableBlock var2 = vars.get(1);
        assertEquals("${  variables.var1 }", var2.getComplete());
        assertEquals("variables.var1", var2.getName());
        assertEquals("${  variables.var1 }", content.subSequence(var2.getStartIndex(), var2.getEndIndex()));

    }

}
