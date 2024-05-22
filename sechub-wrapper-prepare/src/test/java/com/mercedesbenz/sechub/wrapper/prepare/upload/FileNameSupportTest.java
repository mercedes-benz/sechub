package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.TestFileWriter;

class FileNameSupportTest {

    FileNameSupport fileNameSupportToTest;

    TestFileWriter writer;

    @BeforeEach
    void beforeEach() {
        fileNameSupportToTest = new FileNameSupport();
        writer = new TestFileWriter();
    }

    @Test
    void getSubfolderFromDirectory_returns_subfolder() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-file-support").toFile();
        tempDir.deleteOnExit();

        String path = tempDir.getAbsolutePath();
        String subfolder = "/subfolder";
        String file = subfolder + "/test";
        writer.save(new File(path + file), "testText", true);

        /* execute */
        String result = fileNameSupportToTest.getSubfolderFileNameFromDirectory(path);

        /* test */
        assertEquals(subfolder, "/" + result);
    }

    @Test
    void getSubfolderFromDirectory_throws_error_when_more_than_one_subfolder() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-file-support").toFile();
        tempDir.deleteOnExit();

        String path = tempDir.getAbsolutePath();
        String subfolder1 = "/subfolder/hello";
        String subfolder2 = "/subfolder2/hello";
        writer.save(new File(path + subfolder1), "testText", true);
        writer.save(new File(path + subfolder2), "testText", true);

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileNameSupportToTest.getSubfolderFileNameFromDirectory(path);
        });

        /* test */
        assertTrue(exception.getMessage().contains("Download directory contains more than one subfolder"));
    }

    @Test
    void getSubfolderFromDirectory_throws_error_when_directory_is_empty() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-file-support").toFile();
        tempDir.deleteOnExit();

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileNameSupportToTest.getSubfolderFileNameFromDirectory(tempDir.getAbsolutePath());
        });

        /* test */
        assertTrue(exception.getMessage().contains("Download directory is empty"));
    }

}