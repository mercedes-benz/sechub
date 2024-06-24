// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.environment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry.EnvironmentVariableKeyValueEntry;
import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry.EnvironmentVariableKeyValueEntryBuilder;

class SecureEnvironmentVariableKeyValueRegistryTest {

    private SecureEnvironmentVariableKeyValueRegistry registryToTest;

    @BeforeEach
    void beforeEach() {
        registryToTest = new SecureEnvironmentVariableKeyValueRegistry();
    }

    @Test
    void key_with_value_defined_as_nullable_and_not_nullable_throws_illegal_state_exception_at_registration_time() {
        /* prepare */
        EnvironmentVariableKeyValueEntryBuilder builder = registryToTest.newEntry().key("alpha.beta.gamma").notNullValue("x").nullableValue("x");

        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registryToTest.register(builder);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("Two ways of value definition used"));
    }

    @Test
    void key_with_NULL_value_register_throws_illegal_argument_exception_at_registration_time() {
        /* prepare */
        EnvironmentVariableKeyValueEntryBuilder builder = registryToTest.newEntry().key("alpha.beta.gamma").notNullValue(null);

        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registryToTest.register(builder);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("was null"));
    }

    @Test
    void only_key_used_at_registration_fails_with_message() {
        /* prepare */
        EnvironmentVariableKeyValueEntryBuilder builder = registryToTest.newEntry().key("alpha.beta.gamma");

        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> registryToTest.register(builder));

        String message = exception.getMessage();
        assertTrue(message.contains("No value defined at all"));

    }

    @Test
    void only_value_used_at_registration_fails_with_illegal_argument_exception() {
        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> registryToTest.register(registryToTest.newEntry().notNullValue("only-value")));

        String message = exception.getMessage();
        assertTrue(message.contains("key not defined"));

    }

    @Test
    void key_with_notNullValue_register_results_in_entry_with_key_value_and_generated_env_variable() {
        /* execute */
        registryToTest.register(registryToTest.newEntry().key("alpha.beta.gamma").notNullValue("the-value"));

        /* test */
        List<EnvironmentVariableKeyValueEntry> entries = registryToTest.getEntries();
        assertEquals(1, entries.size());
        EnvironmentVariableKeyValueEntry entry = entries.iterator().next();

        assertEquals("alpha.beta.gamma", entry.getKey());
        assertEquals("ALPHA_BETA_GAMMA", entry.getVariableName());
        assertEquals("the-value", entry.getValue());

    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "value1" })
    void key_with_NullableValue_register_results_in_entry_with_key_value_and_generated_env_variable(String value) {
        /* execute */
        registryToTest.register(registryToTest.newEntry().key("alpha.beta.gamma").nullableValue(value));

        /* test */
        List<EnvironmentVariableKeyValueEntry> entries = registryToTest.getEntries();
        assertEquals(1, entries.size());
        EnvironmentVariableKeyValueEntry entry = entries.iterator().next();

        assertEquals("alpha.beta.gamma", entry.getKey());
        assertEquals("ALPHA_BETA_GAMMA", entry.getVariableName());
        assertEquals(value, entry.getValue());

    }

    @Test
    void key_with_value_and_env_variable__register_results_in_entry_with_key_value_and_defined_env_variable() {
        /* execute */
        registryToTest.register(registryToTest.newEntry().key("alpha.beta.gamma").variable("OTHER_ENV_VARIABLE").notNullValue("the-value"));

        /* test */
        List<EnvironmentVariableKeyValueEntry> entries = registryToTest.getEntries();
        assertEquals(1, entries.size());
        EnvironmentVariableKeyValueEntry entry = entries.iterator().next();

        assertEquals("alpha.beta.gamma", entry.getKey());
        assertEquals("OTHER_ENV_VARIABLE", entry.getVariableName());
        assertEquals("the-value", entry.getValue());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "i am illegal because with spaces", " with.leading.space" })
    void new_entry_build_illegal_key_throws_illegal_argument_exception(String illegalKey) {
        assertThrows(IllegalArgumentException.class,
                () -> registryToTest.newEntry().key(illegalKey).variable("OTHER_ENV_VARIABLE").notNullValue("the-value").build());
    }

}
