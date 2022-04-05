// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

class ProjectTest {

    @Test
    void a_new_project_has_access_level_full() {
        assertEquals(ProjectAccessLevel.FULL, new Project().getAccessLevel());
    }

}
