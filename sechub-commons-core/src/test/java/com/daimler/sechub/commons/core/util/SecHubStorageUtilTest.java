// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SecHubStorageUtilTest {

    @Test
    public void jobstorage_path_for_porject_with_id_project1234_is_correct_calculated() {
        assertEquals("jobstorage/project1234", SecHubStorageUtil.createStoragePath("project1234"));
    }

}
