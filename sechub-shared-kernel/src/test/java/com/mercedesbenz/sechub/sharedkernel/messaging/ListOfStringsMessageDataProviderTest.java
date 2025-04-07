// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class ListOfStringsMessageDataProviderTest {

    private ListOfStringsMessageDataProvider providerToTest = new ListOfStringsMessageDataProvider();

    @Test
    void null_handled_correctly() {
        /* execute */
        List<String> profileIds = providerToTest.get(null);
        String profileIdsAsString = providerToTest.getString(null);

        /* test */
        assertThat(profileIds).isNull();
        assertThat(profileIdsAsString).isNull();
    }

    @Test
    void empty_values_handled_correctly() {
        /* execute */
        List<String> profileIds = providerToTest.get("[]");
        String profileIdsAsString = providerToTest.getString(Collections.emptyList());

        /* test */
        assertThat(profileIds).isEmpty();
        assertThat(profileIdsAsString).isEqualTo("[]");
    }

    @Test
    void non_empty_data_handled_correctly() {
        /* prepare */
        String profile1 = "profile1";
        String profile2 = "profile2";

        /* execute */
        String profileIdsAsString = providerToTest.getString(List.of(profile1, profile2));
        List<String> profileIds = providerToTest.get(profileIdsAsString);

        /* test */
        assertThat(profileIds).contains(profile1, profile2);
    }

}
