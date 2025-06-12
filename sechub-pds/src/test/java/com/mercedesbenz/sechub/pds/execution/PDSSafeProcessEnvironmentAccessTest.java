// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PDSSafeProcessEnvironmentAccessTest {

    private Map<String, String> environment;

    private PDSSafeProcessEnvironmentAccess pdsSafeProcessEnvironmentAccess;

    @BeforeEach
    void beforeEach() {
        environment = mock();

        pdsSafeProcessEnvironmentAccess = new PDSSafeProcessEnvironmentAccess(environment);
    }

    @Test
    void put_null_key_ignored() {
        /* prepare */
        String key = null;
        String value = "someValue";

        /* execute */
        pdsSafeProcessEnvironmentAccess.put(key, value);

        /* test */
        verifyNoInteractions(environment);
    }

    @Test
    void put_null_value_ignored() {
        /* prepare */
        String key = "someKey";
        String value = null;

        /* execute */
        pdsSafeProcessEnvironmentAccess.put(key, value);

        /* test */
        verifyNoInteractions(environment);
    }

    @Test
    void put_valid_key_value__does_put_on_original_map() {
        /* prepare */
        String key = "someKey";
        String value = "someValue";

        /* execute */
        pdsSafeProcessEnvironmentAccess.put(key, value);

        /* test */
        verify(environment).put(key, value);
    }

    @Test
    void get_keys_fetchs_keys_from_original_map() {
        /* prepare */
        Map<String, String> env = new LinkedHashMap<>();

        env.put("key1", "value1");
        env.put("key2", "value2");
        pdsSafeProcessEnvironmentAccess = new PDSSafeProcessEnvironmentAccess(env);

        /* execute */
        Set<String> keys = pdsSafeProcessEnvironmentAccess.getKeys();

        /* test */
        assertThat(keys).containsExactlyInAnyOrder("key1", "key2");
    }

    @Test
    void get_existing_key__returns_value_from_original_map() {
        /* prepare */
        String key = "someKey";
        String value = "someValue";
        when(environment.get(key)).thenReturn(value);

        /* execute */
        String result = pdsSafeProcessEnvironmentAccess.get(key);

        /* test */
        assertThat(result).isEqualTo(value);
    }

    @Test
    void get_non_existing_key__returns_null_from_original_map() {
        /* prepare */
        String key = "nonExistingKey";
        when(environment.get(key)).thenReturn(null);

        /* execute */
        String result = pdsSafeProcessEnvironmentAccess.get(key);

        /* test */
        assertThat(result).isNull();
    }
}