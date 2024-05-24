package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
    void getRepositoriesFromDirectory_returns_subfolder() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-file-support").toFile();
        tempDir.deleteOnExit();

        String subfolder = "/subfolder";
        String file = subfolder + "/test";
        writer.save(new File(tempDir + file), "testText", true);

        /* execute */
        List<Path> result = fileNameSupportToTest.getRepositoriesFromDirectory(tempDir.toPath());

        /* test */
        assertEquals(subfolder, "/" + result.get(0).getFileName());
        assertEquals(1, result.size());
        assertEquals(tempDir + subfolder, result.get(0).toString());
    }

    @Test
    void getRepositoriesFromDirectory_returns_multiple_directories() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-file-support").toFile();
        tempDir.deleteOnExit();

        String path = tempDir.getAbsolutePath();
        String subfolder1 = "/subfolder";
        String subfolder2 = "/subfolder2";
        writer.save(new File(path + subfolder1 + "/hello"), "testText", true);
        writer.save(new File(path + subfolder2 + "/hello"), "testText", true);

        /* execute */
        List<Path> result = fileNameSupportToTest.getRepositoriesFromDirectory(tempDir.toPath());

        /* test */
        assertEquals(2, result.size());
    }

    @Test
    void getRepositoriesFromDirectory_returns_empty_list_when_empty() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-file-support").toFile();
        tempDir.deleteOnExit();

        /* execute */
        List<Path> result = fileNameSupportToTest.getRepositoriesFromDirectory(tempDir.toPath());

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void getTarFilesFromDirectory_returns_tar_file() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-file-support").toFile();
        tempDir.deleteOnExit();

        String path = tempDir.getAbsolutePath();
        String tarFile = "/test-tar-file.tar";
        writer.save(new File(path + tarFile), "testText", true);

        /* execute */
        List<Path> result = fileNameSupportToTest.getTarFilesFromDirectory(tempDir.toPath());

        /* test */
        assertEquals(1, result.size());
        assertEquals(tarFile, "/" + result.get(0).getFileName());
        assertEquals(tempDir + tarFile, result.get(0).toString());
    }

    @Test
    void getTarFilesFromDirectory_returns_multiple_tar_files() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-file-support").toFile();
        tempDir.deleteOnExit();

        String path = tempDir.getAbsolutePath();
        String tarFile1 = "/test-tar-file1.tar";
        String tarFile2 = "/test-tar-file2.tar";
        writer.save(new File(path + tarFile1), "testText", true);
        writer.save(new File(path + tarFile2), "testText", true);

        /* execute */
        List<Path> result = fileNameSupportToTest.getTarFilesFromDirectory(tempDir.toPath());

        /* test */
        assertEquals(2, result.size());
    }

    @Test
    void getTarFilesFromDirectory_returns_empty_list_when_empty() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-file-support").toFile();
        tempDir.deleteOnExit();

        /* execute */
        List<Path> result = fileNameSupportToTest.getTarFilesFromDirectory(tempDir.toPath());

        /* test */
        assertTrue(result.isEmpty());
    }
}