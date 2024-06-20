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

class SecureEnvironmentVariableKeyValueRegistryTest {

    private SecureEnvironmentVariableKeyValueRegistry registryToTest;

    @BeforeEach
    void beforeEach() {
        registryToTest = new SecureEnvironmentVariableKeyValueRegistry();
    }

    @Test
    void key_with_value_register_results_in_entry_with_key_value_and_generated_env_variable() {
        /* execute */
        registryToTest.register(registryToTest.newEntry().key("alpha.beta.gamma").value("the-value"));

        /* test */
        List<EnvironmentVariableKeyValueEntry> entries = registryToTest.getEntries();
        assertEquals(1, entries.size());
        EnvironmentVariableKeyValueEntry entry = entries.iterator().next();

        assertEquals("alpha.beta.gamma", entry.getKey());
        assertEquals("ALPHA_BETA_GAMMA", entry.getVariableName());
        assertEquals("the-value", entry.getValue());

    }

    @Test
    void key_with_value_and_env_variable__register_results_in_entry_with_key_value_and_defined_env_variable() {
        /* execute */
        registryToTest.register(registryToTest.newEntry().key("alpha.beta.gamma").variable("OTHER_ENV_VARIABLE").value("the-value"));

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
        assertThrows(IllegalArgumentException.class, () -> registryToTest.newEntry().key(illegalKey).variable("OTHER_ENV_VARIABLE").value("the-value").build());
    }

}
