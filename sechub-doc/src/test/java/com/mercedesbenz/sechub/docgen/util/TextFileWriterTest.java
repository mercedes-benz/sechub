// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.TestUtil;

public class TextFileWriterTest {

    private DocGenTextFileWriter writerToTest;

    @BeforeEach
    void before() throws Exception {
        writerToTest = new DocGenTextFileWriter();
    }

    @Test
    void is_able_to_save_a_file_having_path_not_createad_before() throws Exception {
        /* prepare */
        File file = TestUtil.createTempDirectoryInBuildFolder("textfilewriter-test").toFile();
        File subFolder = new File(file, "subFolder");
        File targetFile = new File(subFolder, "targetFile");
        targetFile.deleteOnExit();

        assertFalse(subFolder.exists());
        assertFalse(targetFile.exists());

        /* execute */
        writerToTest.writeTextToFile(targetFile, "text");

        /* test */
        assertTrue(subFolder.exists());
        assertTrue(targetFile.exists());

        try (BufferedReader br = new BufferedReader(new FileReader(targetFile))) {
            String line = br.readLine();
            assertEquals("text", line);
        }
    }
}
