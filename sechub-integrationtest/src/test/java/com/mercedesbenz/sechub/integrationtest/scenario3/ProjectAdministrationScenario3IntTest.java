// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario3;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDefaultProfiles.*;
import static com.mercedesbenz.sechub.integrationtest.scenario3.Scenario3.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.domain.administration.project.ProjectData;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.TextSearchMode;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario3.class)
public class ProjectAdministrationScenario3IntTest {

    /**
     * Why is this test method testing so much? It was settled inside 4 different
     * tests which do nearly execute the same action, but with the necessary
     * execution and test time the test did run with 28 seconds! Now this single
     * test does test the same but in 12 seconds.
     */
    @Test
    /* @formatter:off */
    void change_project_ownership_by_admin_and_owners() {
        /* check precondition */
        assertUser(USER_1).
            isAssignedToProject(PROJECT_1).
            isOwnerOf(PROJECT_1).
            isOwnerOf(PROJECT_2);

        assertUser(USER_2).
            isNotOwnerOf(PROJECT_1).
            isNotAssignedToProject(PROJECT_2).
            hasNotOwnerRole();

        /* execute 1 - assign user 3 and change ownership of project 1*/
        as(SUPER_ADMIN).
            assignUserToProject(USER_3, PROJECT_1). // do this to have this user assigned before ownership change later
            changeProjectOwnerOfProject(USER_2, PROJECT_1);

        /* test 1*/
        String subject = "Owner of project .* changed";

        assertUser(USER_1).
            hasReceivedEmail(subject, TextSearchMode.REGULAR_EXPRESSON).
            isNotOwnerOf(PROJECT_1). // lost ownership
            isAssignedToProject(PROJECT_1). // still assigned after ownership loss
            isOwnerOf(PROJECT_2). // still owner of project2
            hasOwnerRole(); // has still owner role

        assertUser(USER_2).
            hasReceivedEmail(subject, TextSearchMode.REGULAR_EXPRESSON).
            isOwnerOf(PROJECT_1). // owner changed
            isAssignedToProject(PROJECT_1). //because of owner change now assigned
            hasOwnerRole(); // is now role owner

        assertUser(USER_3).
            hasReceivedEmail(subject, TextSearchMode.REGULAR_EXPRESSON).
            isAssignedToProject(PROJECT_1);

        /* test + execute 2 : check user 1 is no longer able to change owner ship of project 1*/
        expectHttpFailure(()->as(USER_1).changeProjectOwnerOfProject(USER_3, PROJECT_1), HttpStatus.FORBIDDEN);

        /* test + execute 2 : check user 2 is able to change owner ship of project 1*/
        as(USER_2).
            changeProjectOwnerOfProject(USER_3, PROJECT_1);// user3 was already assigned, here we test ownership change is possible for already assigned users

        /* test 3 - ownership has been transfered by former owner (USER_2) itself to USER 3*/
        assertUser(USER_3).
            isOwnerOf(PROJECT_1).
            isAssignedToProject(PROJECT_1).
            isNotAssignedToProject(PROJECT_2).
            hasOwnerRole();

        assertUser(USER_2).
            isNotOwnerOf(PROJECT_1).
            isNotOwnerOf(PROJECT_2).
            isAssignedToProject(PROJECT_1). // still assigned
            isNotAssignedToProject(PROJECT_2). // still not assigned
            hasNotOwnerRole(); // no longer a role owner

        /* super admin can remove ower from project */
        as(SUPER_ADMIN).
            unassignUserFromProject(USER_1, PROJECT_2);

        assertUser(USER_1).
            isOwnerOf(PROJECT_2). // lost ownership
            isNotAssignedToProject(PROJECT_2). // still assigned after ownership loss
            hasOwnerRole(); // has still owner role

        /* test 4 - project details contain profile IDs */

        // normal user can view profiles of assigned projects
        List<ProjectData> projectDetailsOfnormalUser = as(USER_2).getAssignedProjectDataList();
        assertThat(projectDetailsOfnormalUser).hasSize(1);
        Set<String> userAssignedProfileIds = projectDetailsOfnormalUser.get(0).getAssignedProfileIds();
        assertThat(userAssignedProfileIds).containsExactly(PROFILE_1.id);

        // owner can view profiles of assigned projects
        List<ProjectData> projectDetailsOfOwner = as(USER_1).getAssignedProjectDataList();
        assertThat(projectDetailsOfOwner).hasSize(1);
        Set<String> ownerAssignedProfileIds = projectDetailsOfOwner.get(0).getAssignedProfileIds();
        assertThat(ownerAssignedProfileIds).containsExactly(PROFILE_1.id);

        // admin can view profiles of assigned projects
        assertThat(as(SUPER_ADMIN).getAssignedProjectDataList()).isEmpty();

    }
    /* @formatter:on */

}
