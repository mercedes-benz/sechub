// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;

public class TestUtilTest {

    @Test
    public void create_random_string_creates_string_with_given_length_test_with_some_examples() {
        assertEquals(0, TestUtil.createRAndomString(0).length());
        assertEquals(1, TestUtil.createRAndomString(1).length());
        assertEquals(61, TestUtil.createRAndomString(61).length());
        assertEquals(255, TestUtil.createRAndomString(255).length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_random_string_throws_illegal_argument_exception_when_negative_1_length() {
        TestUtil.createRAndomString(-1);
    }

    @Test
    public void createTempDirectory() throws IOException {
        Path created = TestUtil.createTempDirectoryInBuildFolder("test-create-tempdir");

        assertTrue("Not existing path:" + created, created.toFile().exists());
    }

}
