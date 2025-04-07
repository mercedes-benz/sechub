// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class ProjectUserDataTest {

    @Test
    void a_sorted_set_contains_user_data_sorted_by_user_id() {
        /* prepare */
        SortedSet<ProjectUserData> sortedSet = new TreeSet<>();

        ProjectUserData data1 = new ProjectUserData();
        data1.setUserId("user1");
        data1.setEmailAddress("2@example.org");

        ProjectUserData data2 = new ProjectUserData();
        data2.setUserId("user2");
        data2.setEmailAddress("3@example.org");

        ProjectUserData data3 = new ProjectUserData();
        data3.setUserId("user3");
        data3.setEmailAddress("1@example.org");

        /* execute */
        sortedSet.addAll(Set.of(data3, data1, data2));

        /* test */
        assertThat(sortedSet).hasSize(3).containsExactly(data1, data2, data3);
    }

    @Test
    void equals_is_correct_implemented() {
        /* prepare */
        ProjectUserData user1 = new ProjectUserData();
        user1.setUserId("user1");
        user1.setEmailAddress("user1@example.org");

        ProjectUserData user2 = new ProjectUserData();
        user2.setUserId("user2");
        user2.setEmailAddress("user2@example.org");

        ProjectUserData user1ButOtherMailAddress = new ProjectUserData();
        user1ButOtherMailAddress.setUserId("user1");
        user1ButOtherMailAddress.setEmailAddress("changed-mail-address@example.org");

        /* execute + test */

        assertThat(user1).isEqualTo(user1);
        assertThat(user1).isNotEqualTo(user2);
        assertThat(user2).isNotEqualTo(user1);
        assertThat(user1).isEqualTo(user1ButOtherMailAddress);
        assertThat(user2).isNotEqualTo(user1ButOtherMailAddress);

    }

}
