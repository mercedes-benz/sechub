// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class MapStringToListOfStringsMessageDataProviderTest {

    private MapStringToListOfStringsMessageDataProvider providerToTest = new MapStringToListOfStringsMessageDataProvider();

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
        Map<String, List<String>> projectToProfiles = providerToTest.get(json);

        /* test */
        assertThat(projectToProfiles).isNull();
    }

    @Test
    void empty_values_handled_correctly_on_getString() {
        /* execute */
        String profileIdsAsString = providerToTest.getString(Collections.emptyMap());

        /* test */
        assertThat(profileIdsAsString).isEqualTo("{}");
    }

    @Test
    void empty_values_handled_correctly_on_get() {
        /* execute */
        Map<String, List<String>> projectToProfiles = providerToTest.get("{}");

        /* test */
        assertThat(projectToProfiles).isEmpty();
    }

    @Test
    void non_empty_data_handled_correctly_on_getString() {
        /* prepare */
        String project1 = "project1";
        String project2 = "project2";
        List<String> profileList1 = List.of("profile1", "profile2", "profile3");
        List<String> profileList2 = List.of("profile5", "profile7", "profile9");

        Map<String, List<String>> projectToProfiles = Map.of(project1, profileList1, project2, profileList2);

        /* execute */
        String profileIdsAsString = providerToTest.getString(projectToProfiles);

        /* test */
        /* @formatter:off */
        assertThat(profileIdsAsString).contains(project1)
                                      .contains(project2)
                                      .contains(profileList1)
                                      .contains(profileList2);
        /* @formatter:on */
    }

    @Test
    void non_empty_data_handled_correctly_on_get() {
        /* prepare */
        String project1 = "project1";
        String project2 = "project2";
        List<String> profileList1 = List.of("profile1", "profile2", "profile3");
        List<String> profileList2 = List.of("profile5", "profile7", "profile9");

        String json = """
                {"project1":["profile1","profile2","profile3"],"project2":["profile5","profile7","profile9"]}
                """;

        /* execute */
        Map<String, List<String>> transformedResult = providerToTest.get(json);

        /* test */
        List<String> list1 = transformedResult.get(project1);
        List<String> list2 = transformedResult.get(project2);
        assertThat(list1).containsAll(profileList1);
        assertThat(list2).containsAll(profileList2);
    }

}
