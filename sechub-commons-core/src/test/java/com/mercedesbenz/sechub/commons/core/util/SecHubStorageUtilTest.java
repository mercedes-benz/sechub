// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SecHubStorageUtilTest {

    @Test
    void jobstorage_path_for_project_with_id_project1234_is_correct_calculated() {
        assertEquals("jobstorage/project1234", SecHubStorageUtil.createStoragePath("project1234"));
    }

}
