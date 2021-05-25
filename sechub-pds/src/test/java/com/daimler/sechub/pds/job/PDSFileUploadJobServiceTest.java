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
import com.daimler.sechub.pds.util.PDSZipSupport;
import com.daimler.sechub.storage.core.JobStorage;

public class PDSFileUploadJobServiceTest {

    private static final String CONTENT_DATA = "content data";
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
    private PDSZipSupport zipSupport;

    @BeforeEach
    void beforeEach() throws Exception {
        tmpUploadPath = Files.createTempDirectory("pds-upload");
        jobUUID = UUID.randomUUID();
        checksumService = mock(PDSFileChecksumSHA256Service.class);
        workspaceService = mock(PDSWorkspaceService.class);
        storageService = mock(PDSMultiStorageService.class);
        zipSupport = mock(PDSZipSupport.class);

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
        serviceToTest.zipSupport = zipSupport;

        when(checksumService.hasCorrectChecksum(eq(ACCEPTED_CHECKSUM), any())).thenReturn(true);
        when(checksumService.hasCorrectChecksum(eq(NOT_ACCEPTED_CHECKSUM), any())).thenReturn(false);
    }

    @Test
    void upload_works_when_uploads_valid_zipfile_so_given_content_is_given_to_storage() throws Exception {
        /* prepare */
        String result = CONTENT_DATA;
        ExtendedMockMultipartFile multiPart = new ExtendedMockMultipartFile("file", result.getBytes());
        String allowedNameWithMaxLength = "123456789-123456789_123456789.123456.zip";

        when(zipSupport.isZipFile(any())).thenReturn(true);

        /* execute */
        serviceToTest.upload(jobUUID, allowedNameWithMaxLength, multiPart, ACCEPTED_CHECKSUM);

        /* test */
        InputStream usedFileInputStream = multiPart.getRememberedInputStream();
        assertNotNull("File input stream was not fetched!", usedFileInputStream);
        verify(storage).store(eq("123456789-123456789_123456789.123456.zip"), eq(usedFileInputStream));

    }

    @Test
    void upload_works_when_uploads_is_not_valid_zipfile_but_ends_not_with_zip_so_given_content_is_given_to_storage() throws Exception {
        /* prepare */
        String result = CONTENT_DATA;
        ExtendedMockMultipartFile multiPart = new ExtendedMockMultipartFile("file", result.getBytes());
        String allowedNameWithMaxLength = "123456789-123456789_123456789.1234561234";

        /* execute */
        serviceToTest.upload(jobUUID, allowedNameWithMaxLength, multiPart, ACCEPTED_CHECKSUM);

        /* test */
        InputStream usedFileInputStream = multiPart.getRememberedInputStream();
        assertNotNull("File input stream was not fetched!", usedFileInputStream);
        verify(storage).store(eq("123456789-123456789_123456789.1234561234"), eq(usedFileInputStream));

    }

    @Test
    void upload_fails_when_all_correct_but_job_not_found_throws_pds_not_found_exception() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "1234567890123456789012345678901234567890";

        PDSNotFoundException exception = assertThrows(PDSNotFoundException.class, () -> {

            /* execute */
            UUID notExistingJobUUID = UUID.randomUUID();
            serviceToTest.upload(notExistingJobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);

        });

        /* test */
        assertTrue(exception.getMessage().contains("Given job does not exist"));

    }

    @Test
    void upload_fails_when_all_correct_job_found_but_in_state_ready_to_start_throws_illegal_argument_exception() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "1234567890123456789012345678901234567890";
        job.setState(PDSJobStatusState.READY_TO_START);

        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        });

        /* test */
        assertTrue(exception.getMessage().contains("accepted is only:[CREATED]"));

    }

    @Test
    void upload_fails_when_containing_filename_length_41_so_filename_length_too_long_throws_illegal_argument_exception() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "12345678901234567890123456789012345678901";
        assertEquals(41, fileName.length());// check test string has really 41 (just a sanity check)

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        });

        /* test */
        assertTrue(exception.getMessage().contains("40"));
    }

    @Test
    public void upload_fails_when_containing_filename_with_slash_throws_illegal_argument_exception() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "123456789/123456789012345678901234567890";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        });

        /* test */
        assertTrue(exception.getMessage().contains("[a-zA-Z"));
    }

    @Test
    void upload_fails_when_containing_filename_with_backslash_throws_illegal_argument_exception() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "123456789\\123456789012345678901234567890";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        });

        /* test */
        assertTrue(exception.getMessage().contains("[a-zA-Z"));
    }

    @Test
    void upload_fails_when_zipfile_correct_but_checksum_service_says_not_correct_checksum() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String allowedNameWithMaxLength = "123456789-123456789_123456789.123456.zip";

        when(zipSupport.isZipFile(any())).thenReturn(true);

        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, allowedNameWithMaxLength, multiPart, NOT_ACCEPTED_CHECKSUM);
        });

        /* test */
        String message = exception.getMessage();

        assertTrue(message.contains("checksum"));
        assertTrue(message.contains("failed"));
    }

    @Test
    void upload_fails_when_not_a_zipfile_but_checksum_service_says_not_correct_checksum() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String allowedNameWithMaxLength = "123456789-123456789_123456789.123456.txt";

        when(zipSupport.isZipFile(any())).thenReturn(false); // would always fail but may not matter, because not a ZIP file...

        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, allowedNameWithMaxLength, multiPart, NOT_ACCEPTED_CHECKSUM);
        });

        /* test */
        String message = exception.getMessage();

        assertTrue(message.contains("checksum"));
        assertTrue(message.contains("failed"));
    }

    @Test
    void upload_fails_when_filename_ends_with_zip_but_is_not_valid_zip_file() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String allowedNameWithMaxLength = "123456789-123456789_123456789.123456.zip";

        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> {

            /* execute */
            serviceToTest.upload(jobUUID, allowedNameWithMaxLength, multiPart, NOT_ACCEPTED_CHECKSUM);
        });

        /* test */
        String message = exception.getMessage();

        assertTrue(message.contains("zip"));
        assertTrue(message.contains("valid"));
    }

}
