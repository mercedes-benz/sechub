// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

class FileUtilitiesTest {

    @Test
    void file_not_existing_throws_mustexitruntimeexception() {
        /* execute + test */
        assertThrows(ZapWrapperRuntimeException.class, () -> FileUtilities.stringToFile("not-existing-file.json"));
    }

    @Test
    void existing_file_resolved_path_correctly() {
        /* execute */
        File file = FileUtilities.stringToFile("src/test/resources/sechub-config-examples/basic-auth.json");

        /* test */
        assertTrue(file.exists());
        assertEquals("basic-auth.json", file.getName());
    }

}
