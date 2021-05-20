// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.PDSNotFoundException;
import com.daimler.sechub.pds.storage.PDSMultiStorageService;
import com.daimler.sechub.pds.test.ExtendedMockMultipartFile;
import com.daimler.sechub.pds.util.PDSFileChecksumSHA256Service;
import com.daimler.sechub.storage.core.JobStorage;

public class PDSFileUploadJobServiceTest {

    private static final String ACCEPTED_CHECKSUM = "checksum-accepted";
    private static final String NOT_ACCEPTED_CHECKSUM = "checksum-failing";

    private PDSFileUploadJobService serviceToTest;
    private UUID jobUUID;
    private PDSFileChecksumSHA256Service checksumService;
    private Path tmpUploadPath;

    private PDSJobRepository repository;

    private PDSJob job;

    private PDSWorkspaceService workspaceService;
    private PDSMultiStorageService storageService;
    private JobStorage storage;

    @BeforeEach
    void beforeEach() throws Exception {
        tmpUploadPath = Files.createTempDirectory("pds-upload");
        jobUUID = UUID.randomUUID();
        checksumService = mock(PDSFileChecksumSHA256Service.class);
        workspaceService = mock(PDSWorkspaceService.class);
        storageService = mock(PDSMultiStorageService.class);

        storage = mock(JobStorage.class);
        when(storageService.getJobStorage(jobUUID)).thenReturn(storage);

        when(workspaceService.getUploadFolder(jobUUID)).thenReturn(new File(tmpUploadPath.toFile(), jobUUID.toString()));

        repository = mock(PDSJobRepository.class);
        job = new PDSJob();
        job.uUID = jobUUID;

        Optional<PDSJob> jobOption = Optional.of(job);
        when(repository.findById(jobUUID)).thenReturn(jobOption);

        serviceToTest = new PDSFileUploadJobService();
        serviceToTest.checksumService = checksumService;
        serviceToTest.workspaceService = workspaceService;
        serviceToTest.repository = repository;
        serviceToTest.storageService = storageService;

        when(checksumService.hasCorrectChecksum(eq(ACCEPTED_CHECKSUM), any())).thenReturn(true);
        when(checksumService.hasCorrectChecksum(eq(NOT_ACCEPTED_CHECKSUM), any())).thenReturn(false);
    }

    @Test
    void upload_all_correct_but_job_not_found_throws_pds_not_found_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "1234567890123456789012345678901234567890";
        assertEquals(40, fileName.length());// check precondition

        PDSNotFoundException exception = assertThrows(PDSNotFoundException.class, () -> {

            /* execute */
            UUID notExistingJobUUID = UUID.randomUUID();
            serviceToTest.upload(notExistingJobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);

        });

        /* test */
        assertTrue(exception.getMessage().contains("Given job does not exist"));

    }

    @Test
    void upload_all_correct_job_found_but_in_state_ready_to_start_throws_illegal_argument_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "1234567890123456789012345678901234567890";
        assertEquals(40, fileName.length());// check precondition
        job.setState(PDSJobStatusState.READY_TO_START);

        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        });

        /* test */
        assertTrue(exception.getMessage().contains("accepted is only:[CREATED]"));

    }

    @Test
    void upload_containing_filename_length_41_throws_illegal_argument_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "12345678901234567890123456789012345678901";
        assertEquals(41, fileName.length());// check precondition

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        });

        /* test */
        assertTrue(exception.getMessage().contains("40"));
    }

    @Test
    public void upload_containing_filename_with_slash_throws_illegal_argument_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "123456789/123456789012345678901234567890";
        assertEquals(40, fileName.length());// check precondition

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        });


        /* test */
        assertTrue(exception.getMessage().contains("[a-zA-Z"));
    }

    @Test
    void upload_uploads_given_content_to_storage_with_to_specified_path() throws Exception {
        /* prepare */
        String result = "content data";
        ExtendedMockMultipartFile multiPart = new ExtendedMockMultipartFile("file", result.getBytes());
        String allowedNameWithMaxLength = "123456789-123456789_123456789.123456.zip";
        assertEquals(40, allowedNameWithMaxLength.length());

        /* execute */
        serviceToTest.upload(jobUUID, allowedNameWithMaxLength, multiPart, ACCEPTED_CHECKSUM);

        /* test */
        InputStream usedFileInputStream = multiPart.getRememberedInputStream();
        assertNotNull("File input stream was not fetched!", usedFileInputStream);
        verify(storage).store(eq("123456789-123456789_123456789.123456.zip"), eq(usedFileInputStream));

    }

    @Test
    void upload_containing_filename_with_backslash_throws_illegal_argument_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "123456789\\123456789012345678901234567890";
        assertEquals(40, fileName.length());// check precondition

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        });

        /* test */
        assertTrue(exception.getMessage().contains("[a-zA-Z"));
    }

    @Test
    void upload_uploads_given_content_to_file_to_specified_path_fails_when_checksum_service_says_not_correct_checksum() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String allowedNameWithMaxLength = "123456789-123456789_123456789.123456.zip";
        assertEquals(40, allowedNameWithMaxLength.length());

        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, allowedNameWithMaxLength, multiPart, NOT_ACCEPTED_CHECKSUM);
        });

        /* test */
        String message = exception.getMessage();
        
        assertTrue(message.contains("checksum"));
        assertTrue(message.contains("failed"));
    }

}
