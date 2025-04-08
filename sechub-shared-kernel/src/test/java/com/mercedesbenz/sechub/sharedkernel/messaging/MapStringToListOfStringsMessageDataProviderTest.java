// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MapStringToListOfStringsMessageDataProviderTest {

    private MapStringToListOfStringsMessageDataProvider providerToTest = new MapStringToListOfStringsMessageDataProvider();

    @Test
    void null_handled_correctly() {
        /* execute */
        Map<String, List<String>> projectToProfiles = providerToTest.get(null);
        String profileIdsAsString = providerToTest.getString(null);

        /* test */
        assertThat(projectToProfiles).isNull();
        assertThat(profileIdsAsString).isNull();
    }

    @Test
    void empty_values_handled_correctly() {
        /* execute */
        Map<String, List<String>> projectToProfiles = providerToTest.get("{}");
        String profileIdsAsString = providerToTest.getString(Collections.emptyMap());

        /* test */
        assertThat(projectToProfiles).isEmpty();
        assertThat(profileIdsAsString).isEqualTo("{}");
    }

    @Test
    void non_empty_data_handled_correctly() {
        /* prepare */
        String project1 = "project1";
        String project2 = "project2";
        List<String> profileList1 = List.of("profile1", "profile2", "profile3");
        List<String> profileList2 = List.of("profile5", "profile7", "profile9");

        Map<String, List<String>> projectToProfiles = Map.of(project1, profileList1, project2, profileList2);

        /* execute */
        String profileIdsAsString = providerToTest.getString(projectToProfiles);
        Map<String, List<String>> transformedResult = providerToTest.get(profileIdsAsString);

        /* test */
        List<String> list1 = transformedResult.get(project1);
        List<String> list2 = transformedResult.get(project2);
        assertThat(list1).containsAll(profileList1);
        assertThat(list2).containsAll(profileList2);
    }

}
