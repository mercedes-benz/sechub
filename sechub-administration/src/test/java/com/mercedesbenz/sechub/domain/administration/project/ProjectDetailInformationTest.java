// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

class ProjectDetailInformationTest {

    @Test
    void constructor_stores_data_from_project_into_fields() {
        /* prepare */
        Project project = mock(Project.class);

        User owner = mock(User.class);
        when(owner.getName()).thenReturn("owner1");

        User user1 = mock(User.class);
        when(user1.getName()).thenReturn("user1");

        User user2 = mock(User.class);
        when(user2.getName()).thenReturn("user2");

        String projectId = "id1";
        ProjectMetaDataEntity metaDataEntity = new ProjectMetaDataEntity(projectId, "key1", "value1");
        ProjectAccessLevel accessLevel = ProjectAccessLevel.FULL;

        when(project.getWhiteList()).thenReturn(Set.of(URI.create("https://example.com")));
        when(project.getAccessLevel()).thenReturn(accessLevel);
        when(project.getId()).thenReturn(projectId);
        when(project.getOwner()).thenReturn(owner);
        when(project.getMetaData()).thenReturn(Set.of(metaDataEntity));
        when(project.getUsers()).thenReturn(Set.of(user1, user2));
        when(project.getTemplates()).thenReturn(Set.of("template1", "template2"));

        /* execute */
        ProjectDetailInformation toTest = new ProjectDetailInformation(project);

        /* test */
        assertThat(toTest.getProjectId()).isEqualTo(projectId);
        assertThat(toTest.getAccessLevel()).isEqualTo(accessLevel.getId());
        assertThat(toTest.getOwner()).isEqualTo("owner1");
        assertThat(toTest.getUsers()).contains("user1", "user2");
        assertThat(toTest.getWhiteList()).contains("https://example.com");
        assertThat(toTest.getTemplates()).contains("template1", "template2");
        assertThat(toTest.getMetaData()).containsEntry("key1", "value1");

    }

}
