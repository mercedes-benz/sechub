// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class SecHubStorageUtilTest {

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "project1234", "p" })
    void jobstorage_path_correct_calculated_for_project_with_id_project1234(String projectId) {
        assertEquals("jobstorage/" + projectId, SecHubStorageUtil.createStoragePathForProject(projectId));
    }

}
