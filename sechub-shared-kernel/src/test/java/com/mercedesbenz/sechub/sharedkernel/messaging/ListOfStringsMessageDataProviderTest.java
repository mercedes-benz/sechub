// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ListOfStringsMessageDataProviderTest {

    private ListOfStringsMessageDataProvider providerToTest = new ListOfStringsMessageDataProvider();

    @Test
    void null_handled_correctly_on_getString() {
        /* execute */
        String profileIdsAsString = providerToTest.getString(null);

        /* test */
        assertThat(profileIdsAsString).isNull();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "   ", "\n", "\r", "\r\n" })
    void null_and_blank_strings_handled_correctly_on_get(String json) {
        /* execute */
        List<String> profileIds = providerToTest.get(json);

        /* test */
        assertThat(profileIds).isNull();
    }

    @Test
    void empty_values_handled_correctly_on_getString() {
        /* execute */
        String profileIdsAsString = providerToTest.getString(Collections.emptyList());

        /* test */
        assertThat(profileIdsAsString).isEqualTo("[]");
    }

    @Test
    void empty_values_handled_correctly_on_get() {
        /* execute */
        List<String> profileIds = providerToTest.get("[]");

        /* test */
        assertThat(profileIds).isEmpty();
    }

    @Test
    void non_empty_data_handled_correctly_on_getString() {
        /* prepare */
        String profile1 = "profile1";
        String profile2 = "profile2";

        /* execute */
        String profileIdsAsString = providerToTest.getString(List.of(profile1, profile2));

        /* test */
        assertThat(profileIdsAsString).contains(profile1, profile2);
    }

    @Test
    void non_empty_data_handled_correctly_on_get() {
        /* prepare */
        String profileIdsAsString = """
                ["profile1", "profile2"]
                """;

        /* execute */
        List<String> profileIds = providerToTest.get(profileIdsAsString);

        /* test */
        assertThat(profileIds).contains("profile1", "profile2");
    }

}
