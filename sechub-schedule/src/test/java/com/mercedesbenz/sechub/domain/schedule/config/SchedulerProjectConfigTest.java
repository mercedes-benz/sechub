// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;

class SchedulerProjectConfigTest {

    @Test
    void initial_config_has_full_project_access() {
        /* execute */
        SchedulerProjectConfig config = new SchedulerProjectConfig();

        /* test */
        assertEquals(ProjectAccessLevel.FULL, config.getProjectAccessLevel());
    }

}
