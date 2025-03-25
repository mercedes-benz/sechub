// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.test.TestFileWriter;

class PrepareWrapperFileUploadServiceTest {

    private PrepareWrapperFileUploadService uploadServiceToTest;
    private JobStorage jobStorage;
    private TestFileWriter writer;

    @BeforeEach
    void beforeEach() {
        jobStorage = mock(JobStorage.class);
        PrepareWrapperStorageService storageService = mock(PrepareWrapperStorageService.class);
        when(storageService.createJobStorageForPath(anyString(), any(UUID.class))).thenReturn(jobStorage);

        uploadServiceToTest = new PrepareWrapperFileUploadService(storageService);
        writer = new TestFileWriter();
    }

    @Test
    void uploadFile_throws_exception_when_projectId_is_null() {
        /* prepare */
        String projectId = null;
        UUID jobUUID = UUID.randomUUID();
        String checkSum = "checkSum";
        File file = new File("path");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            uploadServiceToTest.uploadFile(projectId, jobUUID, file, checkSum);
        });

        /* test */
        assertEquals("projectId may not be null or empty.", exception.getMessage());
    }

    @Test
    void uploadFile_throws_exception_when_jobUUID_is_null() {
        /* prepare */
        String projectId = "projectId";
        UUID jobUUID = null;
        String checkSum = "checkSum";
        File file = new File("path");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            uploadServiceToTest.uploadFile(projectId, jobUUID, file, checkSum);
        });

        /* test */
        assertEquals("jobUUID may not be null.", exception.getMessage());
    }

    @Test
    void uploadFile_throws_exception_when_file_is_null() {
        /* prepare */
        String projectId = "projectId";
        UUID jobUUID = UUID.randomUUID();
        String checkSum = "checkSum";
        File file = null;

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            uploadServiceToTest.uploadFile(projectId, jobUUID, file, checkSum);
        });

        /* test */
        assertEquals("file may not be null.", exception.getMessage());
    }

    @Test
    void uploadFile_throws_exception_when_file_does_not_exist() {
        /* prepare */
        String projectId = "projectId";
        UUID jobUUID = UUID.randomUUID();
        String checkSum = "checkSum";
        File file = new File("path");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            uploadServiceToTest.uploadFile(projectId, jobUUID, file, checkSum);
        });

        /* test */
        assertEquals("Upload file does not exist.", exception.getMessage());
    }

    @Test
    void uploadFile_throws_exception_when_checkSum_is_null() throws IOException {
        /* prepare */
        String projectId = "projectId";
        UUID jobUUID = UUID.randomUUID();
        String checkSum = null;

        File tempDir = Files.createTempDirectory("test-sechub_archive-creator").toFile();
        tempDir.deleteOnExit();
        File file = new File(tempDir + "testfile.tar");
        writer.writeTextToFile(file, "testText", true);

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            uploadServiceToTest.uploadFile(projectId, jobUUID, file, checkSum);
        });

        /* test */
        assertEquals("checkSum may not be empty.", exception.getMessage());
    }

    @Test
    void uploadFile_successfully_uploads_binary_file() throws IOException {
        /* prepare */
        String projectId = "projectId";
        UUID jobUUID = UUID.randomUUID();
        String checkSum = "checkSum";

        File tempDir = Files.createTempDirectory("test-sechub_archive-creator").toFile();
        tempDir.deleteOnExit();
        File file = new File(tempDir + "testfile.tar");
        writer.writeTextToFile(file, "someExampleText", true);

        /* execute */
        uploadServiceToTest.uploadFile(projectId, jobUUID, file, checkSum);

        /* test */
        verify(jobStorage, times(3)).store(any(), any(), anyLong());
        verify(jobStorage).store(eq(FILENAME_BINARIES_TAR), any(), anyLong());
        verify(jobStorage).store(eq(FILENAME_BINARIES_TAR_FILESIZE), any(), anyLong());
        verify(jobStorage).store(eq(FILENAME_BINARIES_TAR_CHECKSUM), any(), anyLong());
    }

    @Test
    void uploadFile_successfully_uploads_source_zip_file() throws IOException {
        /* prepare */
        String projectId = "projectId";
        UUID jobUUID = UUID.randomUUID();
        String checkSum = "checkSum";

        File tempDir = Files.createTempDirectory("test-sechub_archive-creator").toFile();
        tempDir.deleteOnExit();
        File file = new File(tempDir + "testfile.zip");
        writer.writeTextToFile(file, "someExampleText", true);

        /* execute */
        uploadServiceToTest.uploadFile(projectId, jobUUID, file, checkSum);

        /* test */
        verify(jobStorage, times(3)).store(any(), any(), anyLong());
        verify(jobStorage).store(eq(FILENAME_SOURCECODE_ZIP), any(), anyLong());
        verify(jobStorage).store(eq(FILENAME_SOURCECODE_ZIP_FILESIZE), any(), anyLong());
        verify(jobStorage).store(eq(FILENAME_SOURCECODE_ZIP_CHECKSUM), any(), anyLong());
    }

}