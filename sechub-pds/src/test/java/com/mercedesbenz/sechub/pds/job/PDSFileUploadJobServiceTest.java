// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.PDSBadRequestException;
import com.mercedesbenz.sechub.pds.PDSNotAcceptableException;
import com.mercedesbenz.sechub.pds.PDSNotFoundException;
import com.mercedesbenz.sechub.pds.UploadSizeConfiguration;
import com.mercedesbenz.sechub.pds.storage.PDSMultiStorageService;
import com.mercedesbenz.sechub.pds.util.PDSArchiveSupportProvider;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.test.TestUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

public class PDSFileUploadJobServiceTest {

    private static final String CONTENT_DATA = "content data";
    private static final String ACCEPTED_CHECKSUM = "checksum-accepted";
    private static final String NOT_ACCEPTED_CHECKSUM = "checksum-failing";

    private PDSFileUploadJobService serviceToTest;
    private UUID jobUUID;
    private Path tmpUploadPath;

    private PDSJobRepository repository;

    private PDSJob job;

    private PDSWorkspaceService workspaceService;
    private PDSMultiStorageService storageService;
    private JobStorage storage;
    private ArchiveSupport archiveSupport;
    private PDSArchiveSupportProvider archiveSupportProvider;
    private UploadSizeConfiguration configuration;
    private CheckSumSupport checkSumSupport;

    @BeforeEach
    void beforeEach() throws Exception {
        tmpUploadPath = TestUtil.createTempDirectoryInBuildFolder("pds-upload");
        jobUUID = UUID.randomUUID();
        workspaceService = mock(PDSWorkspaceService.class);
        storageService = mock(PDSMultiStorageService.class);
        configuration = mock(UploadSizeConfiguration.class);
        checkSumSupport = mock(CheckSumSupport.class);

        when(configuration.getMaxUploadSizeInBytes()).thenReturn(2048L);

        archiveSupportProvider = mock(PDSArchiveSupportProvider.class);
        archiveSupport = mock(ArchiveSupport.class);
        when(archiveSupportProvider.getArchiveSupport()).thenReturn(archiveSupport);

        storage = mock(JobStorage.class);
        when(storageService.createJobStorage(null, jobUUID)).thenReturn(storage);

        when(workspaceService.getUploadFolder(jobUUID)).thenReturn(new File(tmpUploadPath.toFile(), jobUUID.toString()));

        repository = mock(PDSJobRepository.class);
        job = new PDSJob();
        job.uUID = jobUUID;

        Optional<PDSJob> jobOption = Optional.of(job);
        when(repository.findById(jobUUID)).thenReturn(jobOption);

        serviceToTest = new PDSFileUploadJobService();
        serviceToTest.workspaceService = workspaceService;
        serviceToTest.repository = repository;
        serviceToTest.storageService = storageService;
        serviceToTest.archiveSupportProvider = archiveSupportProvider;
        serviceToTest.configuration = configuration;
        serviceToTest.checksumSupport = checkSumSupport;

        when(checkSumSupport.hasCorrectSha256Checksum(eq(ACCEPTED_CHECKSUM), any())).thenReturn(true);
        when(checkSumSupport.hasCorrectSha256Checksum(eq(NOT_ACCEPTED_CHECKSUM), any())).thenReturn(false);
    }

    @Test
    void upload_fails_when_all_correct_but_job_not_found_throws_pds_not_found_exception() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "1234567890123456789012345678901234567890";

        ServletContext context = new MockServletContext();
        HttpServletRequest request = MockMvcRequestBuilders.multipart("https://localhost:1234").file(multiPart).buildRequest(context);
        PDSNotFoundException exception = assertThrows(PDSNotFoundException.class, () -> {

            /* execute */
            UUID notExistingJobUUID = UUID.randomUUID();
            serviceToTest.upload(notExistingJobUUID, fileName, request);

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

        ServletContext context = new MockServletContext();
        HttpServletRequest request = MockMvcRequestBuilders.multipart("https://localhost:1234").file(multiPart).buildRequest(context);

        PDSNotAcceptableException exception = assertThrows(PDSNotAcceptableException.class, () -> {

            /* execute */
            serviceToTest.upload(job.getUUID(), fileName, request);
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

        ServletContext context = new MockServletContext();
        HttpServletRequest request = MockMvcRequestBuilders.multipart("https://localhost:1234").file(multiPart).buildRequest(context);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            /* execute */
            serviceToTest.upload(job.getUUID(), fileName, request);
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

        ServletContext context = new MockServletContext();
        HttpServletRequest request = MockMvcRequestBuilders.multipart("https://localhost:1234").file(multiPart).buildRequest(context);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            /* execute */
            serviceToTest.upload(job.getUUID(), fileName, request);
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

        ServletContext context = new MockServletContext();
        HttpServletRequest request = MockMvcRequestBuilders.multipart("https://localhost:1234").file(multiPart).buildRequest(context);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            /* execute */
            serviceToTest.upload(job.getUUID(), fileName, request);
        });

        /* test */
        assertTrue(exception.getMessage().contains("[a-zA-Z"));
    }

    @Test
    void upload_fails_when_no_x_file_size_header_negative() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "binaries.tar";

        ServletContext context = new MockServletContext();

        /* formatter:off */
        HttpServletRequest request = MockMvcRequestBuilders.multipart("https://localhost:1234").file(multiPart).header(FILE_SIZE_HEADER_FIELD_NAME, "-1")
                .buildRequest(context);
        /* formatter:on */

        PDSBadRequestException exception = assertThrows(PDSBadRequestException.class, () -> {

            /* execute */
            serviceToTest.upload(job.getUUID(), fileName, request);
        });

        /* test */
        assertEquals("The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " cannot be negative.", exception.getMessage());
    }

    @Test
    void upload_fails_when_no_x_file_size_header_invalid_number() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "binaries.tar";

        ServletContext context = new MockServletContext();

        /* formatter:off */
        HttpServletRequest request = MockMvcRequestBuilders.multipart("https://localhost:1234").file(multiPart).header(FILE_SIZE_HEADER_FIELD_NAME, "abcabc")
                .buildRequest(context);
        /* formatter:on */

        PDSBadRequestException exception = assertThrows(PDSBadRequestException.class, () -> {

            /* execute */
            serviceToTest.upload(job.getUUID(), fileName, request);
        });

        /* test */
        assertEquals("The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " is not formatted as a number.", exception.getMessage());
    }
}
