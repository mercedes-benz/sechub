//SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PDSScriptEnvironmentCleanerTest {

    private PDSScriptEnvironmentCleaner cleanerToTest;
    private LinkedHashMap<String, String> environment;

    @BeforeEach
    void beforeEach() {
        cleanerToTest = new PDSScriptEnvironmentCleaner();

        environment = new LinkedHashMap<String, String>();
    }

    @ParameterizedTest
    @ValueSource(strings = { "X", "DATABASE_PASSWORD" })
    void a_key_which_was_in_environment_and_is_not_whitelisted_will_be_removed(String key) {
        /* prepare */
        environment.put(key, "value1");

        /* execute */
        cleanerToTest.clean(environment);

        /* test */
        assertEquals(null, environment.get(key));

    }

    @ParameterizedTest
    @EnumSource(PDSScriptEnvironmentVariableWhitelistDefault.class)
    void a_key_which_was_in_environment_and_is_whitelisted_per_default_will_not_be_removed(PDSScriptEnvironmentVariableWhitelistDefault whitelistDefault) {
        String key = whitelistDefault.name();
        String value = "value-" + key;
        /* prepare */
        environment.put(key, value);

        /* execute */
        cleanerToTest.clean(environment);

        /* test */
        assertEquals(value, environment.get(key));

    }

    @Test
    void two_keys_which_were_in_environment_and_are_whitelisted_explicit_will_not_be_removed_but_other() {

        /* prepare */
        String key1 = "X";
        String key2 = "DATABASE_PASSWORD";
        String key3 = "OTHER";

        String value1 = "value-" + key1;
        String value2 = "value-" + key2;
        String value3 = "value-" + key3;

        environment.put(key1, value1);
        environment.put(key2, value2);
        environment.put(key3, value3);

        cleanerToTest.setWhiteListCommaSeparated(key1 + ", " + key2);

        /* execute */
        cleanerToTest.clean(environment);

        /* test */
        assertEquals(value1, environment.get(key1));
        assertEquals(value2, environment.get(key2));
        assertEquals(null, environment.get(key3));

    }

    @Test
    void a_key_which_was_in_environment_and_is_whitelisted_explicit_will_not_be_removed() {

        /* prepare */
        String key1 = "X";
        String key2 = "OTHER1";
        String key3 = "OTHER2";

        String value1 = "value-" + key1;
        String value2 = "value-" + key2;
        String value3 = "value-" + key3;

        environment.put(key1, value1);
        environment.put(key2, value2);
        environment.put(key3, value3);

        cleanerToTest.setWhiteListCommaSeparated(key1);

        /* execute */
        cleanerToTest.clean(environment);

        /* test */
        assertEquals(value1, environment.get(key1));
        assertEquals(null, environment.get(key2));
        assertEquals(null, environment.get(key3));

    }

    @Test
    void two_keys_which_are_in_environment_and_are_whitelisted_with_asterisk_will_not_be_removed_but_others() {

        /* prepare */
        String key1 = "PDS_STORAGE_S3_1";
        String key2 = "PDS_STORAGE_S3_1";
        String key3 = "PDS_OTHER";
        String key4 = "PDS_STORAGEHOLDER";

        String value1 = "value-" + key1;
        String value2 = "value-" + key2;
        String value3 = "value-" + key3;
        String value4 = "value-" + key4;

        environment.put(key1, value1);
        environment.put(key2, value2);
        environment.put(key3, value3);
        environment.put(key4, value4);

        cleanerToTest.setWhiteListCommaSeparated("PDS_STORAGE_*");

        /* execute */
        cleanerToTest.clean(environment);

        /* test */
        assertEquals(value1, environment.get(key1));
        assertEquals(value2, environment.get(key2));
        assertEquals(null, environment.get(key3));
        assertEquals(null, environment.get(key4));

    }

    @Test
    void just_test_with_literal_default_whitelist_entry_HOSTNAME() {
        /* prepare */
        environment.put("HOSTNAME", "host1");

        /* execute */
        cleanerToTest.clean(environment);

        /* test */
        assertEquals("host1", environment.get("HOSTNAME"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", ",", ",," })
    void illegal_whitelist_entries_works_cleans_former_whitelist(String commaSeparatedWhitelist) {
        /* prepare */
        cleanerToTest.setWhiteListCommaSeparated("X,Y");
        environment.put("X", "value1");
        environment.put("Y", "value2");

        /* check precondition */
        cleanerToTest.clean(environment);
        assertEquals("value1", environment.get("X"));
        assertEquals("value2", environment.get("Y"));

        /* execute */
        cleanerToTest.setWhiteListCommaSeparated(commaSeparatedWhitelist);
        cleanerToTest.clean(environment);

        /* test */
        assertEquals(null, environment.get("X"));
        assertEquals(null, environment.get("Y"));

    }

}
