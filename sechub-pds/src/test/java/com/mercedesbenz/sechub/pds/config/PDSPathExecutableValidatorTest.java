// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.test.TestUtil;

public class PDSPathExecutableValidatorTest {

    private PDSPathExecutableValidator validatorToTest;

    @Before
    public void before() throws Exception {
        validatorToTest = new PDSPathExecutableValidator();
    }

    @Test
    public void null_path_not_valid() {
        assertNotValid(validatorToTest.createValidationErrorMessage(null));
    }

    @Test
    public void empty_path_not_valid() {
        assertNotValid(validatorToTest.createValidationErrorMessage(""));
    }

    @Test
    public void path_existing_but_not_executable_not_valid() throws Exception {
        /* prepare */
        Path tempFile = TestUtil.createTempFileInBuildFolder("pds_executable", "sh");

        /* execute */
        String message = validatorToTest.createValidationErrorMessage(tempFile.toRealPath().toString());

        /* test */
        assertNotValid(message);
        assertTrue(message.contains("but not executable"));
    }

    @Test
    public void path_existing_and_executable_is_valid() throws Exception {
        /* prepare */
        Path tempFile = TestUtil.createTempFileInBuildFolder("pds_executable", "sh",
                PosixFilePermissions.asFileAttribute(Collections.singleton(PosixFilePermission.OWNER_EXECUTE)));

        /* execute */
        String message = validatorToTest.createValidationErrorMessage(tempFile.toRealPath().toString());

        /* test */
        assertValid(message);
    }

    private void assertValid(String message) {
        /* validation message being null means its valid */
        assertNull(message);
    }

    private void assertNotValid(String message) {
        /* validation message not being null means its not valid */
        assertNotNull(message);
    }
}
