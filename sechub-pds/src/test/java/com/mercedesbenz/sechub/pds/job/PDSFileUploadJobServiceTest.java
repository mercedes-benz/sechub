// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport.CheckSumValidationResult;
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
import jakarta.servlet.ServletInputStream;
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
    private PDSServletFileUploadFactory pdsServletFileUploadFactory;
    private HttpServletRequest httpRequest;

    @BeforeEach
    void beforeEach() throws Exception {
        tmpUploadPath = TestUtil.createTempDirectoryInBuildFolder("pds-upload");
        jobUUID = UUID.randomUUID();
        workspaceService = mock(PDSWorkspaceService.class);
        storageService = mock(PDSMultiStorageService.class);
        configuration = mock(UploadSizeConfiguration.class);
        checkSumSupport = mock(CheckSumSupport.class);
        pdsServletFileUploadFactory = mock(PDSServletFileUploadFactory.class);
        httpRequest = mock();

        when(configuration.getMaxUploadSizeInBytes()).thenReturn(2048L);

        archiveSupportProvider = mock(PDSArchiveSupportProvider.class);
        archiveSupport = mock(ArchiveSupport.class);
        when(archiveSupportProvider.getArchiveSupport()).thenReturn(archiveSupport);

        storage = mock(JobStorage.class);
        when(storageService.createJobStorageForPath(null, jobUUID)).thenReturn(storage);

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
        serviceToTest.servletFileUploadFactory = pdsServletFileUploadFactory;

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

    @Test
    void upload_fails_when_x_file_size_header_exceeds_max_file_size() {
        /* prepare */
        String result = CONTENT_DATA;
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "binaries.tar";

        ServletContext context = new MockServletContext();

        /* max upload size with headers is 2048 + 600 = 2648 */
        String fileSize = "2649";
        /* formatter:off */
        HttpServletRequest request = MockMvcRequestBuilders.multipart("https://localhost:1234").file(multiPart).header(FILE_SIZE_HEADER_FIELD_NAME, fileSize)
                .buildRequest(context);
        /* formatter:on */

        PDSBadRequestException exception = assertThrows(PDSBadRequestException.class, () -> {

            /* execute */
            serviceToTest.upload(job.getUUID(), fileName, request);
        });

        /* test */
        assertEquals("The file size in header field %s exceeds the allowed upload size of 2648".formatted(FILE_SIZE_HEADER_FIELD_NAME), exception.getMessage());
    }

    @Test
    public void multipart_to_many_keys_will_throw_bad_request_execption() throws Exception {
        /* prepare */
        String fileName = "binaries.tar";
        String checksumFromUser = "12345";
        InputStream input = new ByteArrayInputStream("AAA".getBytes());
        ServletInputStream inputStream = new DelegatingServletInputStream(input);

        when(httpRequest.getInputStream()).thenReturn(inputStream);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getContentType()).thenReturn("multipart/form-data; boundary=------a-boundary---");

        when(httpRequest.getHeader(FILE_SIZE_HEADER_FIELD_NAME)).thenReturn("612"); // Add 600 bytes for headers.

        JakartaServletFileUpload<?, ?> upload = mock(JakartaServletFileUpload.class);
        when(pdsServletFileUploadFactory.create()).thenReturn(upload);

        FileItemInputIterator itemIterator = mock(FileItemInputIterator.class);
        FileItemInput checksumItemStream = mock(FileItemInput.class);
        FileItemInput fileItemStream = mock(FileItemInput.class);
        FileItemInput additionalItemStream = mock(FileItemInput.class);

        when(itemIterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(itemIterator.next()).thenReturn(fileItemStream).thenReturn(checksumItemStream).thenReturn(additionalItemStream);

        when(fileItemStream.getInputStream()).thenReturn(input);
        when(fileItemStream.getFieldName()).thenReturn("file");

        when(checksumItemStream.getFieldName()).thenReturn("checkSum");
        when(checksumItemStream.getInputStream()).thenReturn(new ByteArrayInputStream(checksumFromUser.getBytes()));

        when(additionalItemStream.getFieldName()).thenReturn("additional");
        when(additionalItemStream.getInputStream()).thenReturn(new ByteArrayInputStream("additional".getBytes()));

        when(upload.getItemIterator(httpRequest)).thenReturn(itemIterator);

        when(checkSumSupport.convertMessageDigestToHex(any())).thenReturn(checksumFromUser);
        when(checkSumSupport.createSha256MessageDigest()).thenReturn(MessageDigest.getInstance("SHA-256"));
        CheckSumValidationResult validationResult = mock();
        when(validationResult.isValid()).thenReturn(true);
        when(checkSumSupport.validateSha256Checksum(checksumFromUser)).thenReturn(validationResult);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.upload(jobUUID, fileName, httpRequest)).hasMessageContaining("Multipart upload must not contain more than");
    }
}
