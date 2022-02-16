// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.project;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProjectAccessLevelTest {

    @Test
    void isEqualOrHigherThan_full() {
        assertTrue(ProjectAccessLevel.FULL.isEqualOrHigherThan(ProjectAccessLevel.FULL));
        assertTrue(ProjectAccessLevel.FULL.isEqualOrHigherThan(ProjectAccessLevel.READ_ONLY));
        assertTrue(ProjectAccessLevel.FULL.isEqualOrHigherThan(ProjectAccessLevel.NONE));
    }

    @Test
    void isEqualOrHigherThan_read_only() {
        assertFalse(ProjectAccessLevel.READ_ONLY.isEqualOrHigherThan(ProjectAccessLevel.FULL));
        assertTrue(ProjectAccessLevel.READ_ONLY.isEqualOrHigherThan(ProjectAccessLevel.READ_ONLY));
        assertTrue(ProjectAccessLevel.READ_ONLY.isEqualOrHigherThan(ProjectAccessLevel.NONE));
    }

    @Test
    void isEqualOrHigherThan_none() {
        for (ProjectAccessLevel level : ProjectAccessLevel.values()) {
            if (level == ProjectAccessLevel.NONE) {
                assertTrue(ProjectAccessLevel.NONE.isEqualOrHigherThan(level));
            } else {
                assertFalse(ProjectAccessLevel.NONE.isEqualOrHigherThan(level));
            }
        }
    }

    @Test // when nothing defined (null) every access level is satisfied (means no access
          // level configuration at all)
    void every_level_isEqualOrHigherThan_null() {
        for (ProjectAccessLevel level : ProjectAccessLevel.values()) {
            assertTrue(level.isEqualOrHigherThan(null));
        }
    }

}
