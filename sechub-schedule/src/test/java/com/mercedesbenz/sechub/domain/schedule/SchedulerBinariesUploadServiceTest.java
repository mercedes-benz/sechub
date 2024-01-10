// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILE_SIZE_HEADER_FIELD_NAME;
import static com.mercedesbenz.sechub.test.JUnitAssertionAddon.assertThrowsExceptionContainingMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.UUID;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.DelegatingServletInputStream;

import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

public class SchedulerBinariesUploadServiceTest {

    private static final String PROJECT1 = "project1";
    private SchedulerBinariesUploadService serviceToTest;
    private CheckSumSupport checkSumSupport;
    private StorageService storageService;
    private UUID randomUuid;
    private ScheduleAssertService assertService;

    private JobStorage storage;
    private HttpServletRequest httpRequest;
    private SchedulerBinariesUploadConfiguration configuration;
    private ServletFileUploadFactory servletFileUploadFactory;
    private DomainMessageService domainMessageService;

    @BeforeEach
    void beforeEach() {
        randomUuid = UUID.randomUUID();

        checkSumSupport = mock(CheckSumSupport.class);
        storageService = mock(StorageService.class);
        assertService = mock(ScheduleAssertService.class);
        storage = mock(JobStorage.class);
        httpRequest = mock(HttpServletRequest.class);
        configuration = mock(SchedulerBinariesUploadConfiguration.class);
        servletFileUploadFactory = mock(ServletFileUploadFactory.class);
        domainMessageService = mock(DomainMessageService.class);

        ScheduleSecHubJob job = new ScheduleSecHubJob();
        when(assertService.assertJob(PROJECT1, randomUuid)).thenReturn(job);
        when(storageService.getJobStorage(PROJECT1, randomUuid)).thenReturn(storage);

        /* attach at service to test */
        serviceToTest = new SchedulerBinariesUploadService();
        serviceToTest.checkSumSupport = checkSumSupport;
        serviceToTest.storageService = storageService;
        serviceToTest.assertService = assertService;
        serviceToTest.configuration = configuration;
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.assertion = mock(UserInputAssertion.class);
        serviceToTest.auditLogService = mock(AuditLogService.class);
        serviceToTest.servletFileUploadFactory = servletFileUploadFactory;
        serviceToTest.domainMessageService = domainMessageService;

    }

    @Test
    void when_no_multipart_in_http_request_a_bad_request_is_returned() {
        /* execute + test */
        assertThrowsExceptionContainingMessage(BadRequestException.class, "did not contain multipart",
                () -> serviceToTest.uploadBinaries(PROJECT1, randomUuid, httpRequest));

    }

    @Test
    void when_illegal_content_bad_request_returned() throws Exception {
        /* prepare */
        String fileContent = "i-am-illegal-content-without boundary or multipart";
        InputStream input = new ByteArrayInputStream(fileContent.getBytes());
        ServletInputStream inputStream = new DelegatingServletInputStream(input);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getContentType()).thenReturn("multipart/");
        when(httpRequest.getInputStream()).thenReturn(inputStream);
        when(servletFileUploadFactory.create()).thenReturn(new JakartaServletFileUpload());

        String numberOfBytes = String.valueOf(fileContent.getBytes().length);
        when(httpRequest.getHeader(FILE_SIZE_HEADER_FIELD_NAME)).thenReturn(numberOfBytes);

        /* execute + test */
        assertThrowsExceptionContainingMessage(BadRequestException.class, "multipart content is not accepted",
                () -> serviceToTest.uploadBinaries(PROJECT1, randomUuid, httpRequest));

    }

    @Test
    void when_x_binary_header_field_not_set() throws IOException {
        /* prepare */
        InputStream input = new ByteArrayInputStream("test".getBytes());
        ServletInputStream inputStream = new DelegatingServletInputStream(input);
        when(httpRequest.getInputStream()).thenReturn(inputStream);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getContentType()).thenReturn("multipart/");

        /* execute + test */
        assertThrowsExceptionContainingMessage(BadRequestException.class, "Header field " + FILE_SIZE_HEADER_FIELD_NAME + " not set.",
                () -> serviceToTest.uploadBinaries(PROJECT1, randomUuid, httpRequest));

        /* test */
        assertNoUploadEvent();
    }

    @Test
    void when_x_binary_header_field_contains_not_a_number() throws IOException {
        /* prepare */
        InputStream input = new ByteArrayInputStream("test".getBytes());
        ServletInputStream inputStream = new DelegatingServletInputStream(input);
        when(httpRequest.getHeader(FILE_SIZE_HEADER_FIELD_NAME)).thenReturn("invalid number");
        when(httpRequest.getInputStream()).thenReturn(inputStream);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getContentType()).thenReturn("multipart/");

        /* execute + test */
        assertThrowsExceptionContainingMessage(BadRequestException.class,
                "The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " is not formatted as a number.",
                () -> serviceToTest.uploadBinaries(PROJECT1, randomUuid, httpRequest));

        /* test */
        assertNoUploadEvent();
    }

    @Test
    void when_x_binary_header_field_is_negative() throws IOException {
        /* prepare */
        InputStream input = new ByteArrayInputStream("test".getBytes());
        ServletInputStream inputStream = new DelegatingServletInputStream(input);
        when(httpRequest.getHeader(FILE_SIZE_HEADER_FIELD_NAME)).thenReturn("-1");
        when(httpRequest.getInputStream()).thenReturn(inputStream);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getContentType()).thenReturn("multipart/");

        /* execute + test */
        assertThrowsExceptionContainingMessage(BadRequestException.class,
                "The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " cannot be negative.",
                () -> serviceToTest.uploadBinaries(PROJECT1, randomUuid, httpRequest));
        /* test */
        assertNoUploadEvent();
    }

    @Test
    void when_x_binary_header_field_is_greater_than_max_upload_size_in_bytes() throws IOException {
        /* prepare */
        InputStream input = new ByteArrayInputStream("test".getBytes());
        ServletInputStream inputStream = new DelegatingServletInputStream(input);

        when(httpRequest.getInputStream()).thenReturn(inputStream);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getContentType()).thenReturn("multipart/");
        when(httpRequest.getHeader(FILE_SIZE_HEADER_FIELD_NAME)).thenReturn("611"); // Add 600 bytes for headers.
        when(servletFileUploadFactory.create()).thenReturn(new JakartaServletFileUpload());

        when(configuration.getMaxUploadSizeInBytes()).thenReturn((long) 10);

        /* execute + test */
        assertThrowsExceptionContainingMessage(BadRequestException.class,
                "The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " exceeds the allowed upload size.",
                () -> serviceToTest.uploadBinaries(PROJECT1, randomUuid, httpRequest));

        /* test */
        assertNoUploadEvent();
    }

    @Test
    void upload_done_but_checksumfailure_will_send_upload_event() throws Exception {
        /* prepare */
        InputStream input = new ByteArrayInputStream("AAA".getBytes());
        ServletInputStream inputStream = new DelegatingServletInputStream(input);

        when(httpRequest.getInputStream()).thenReturn(inputStream);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getContentType()).thenReturn("multipart/form-data; boundary=------a-boundary---");

        when(httpRequest.getHeader(FILE_SIZE_HEADER_FIELD_NAME)).thenReturn("0"); // Add 600 bytes for headers.

        when(configuration.getMaxUploadSizeInBytes()).thenReturn((long) 612);

        JakartaServletFileUpload<?, ?> upload = mock(JakartaServletFileUpload.class);
        when(servletFileUploadFactory.create()).thenReturn(upload);

        FileItemInputIterator itemIterator = mock(FileItemInputIterator.class);
        FileItemInput checksumItemStream = mock(FileItemInput.class);
        FileItemInput fileItemStream = mock(FileItemInput.class);

        when(itemIterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(itemIterator.next()).thenReturn(fileItemStream).thenReturn(checksumItemStream);

        when(fileItemStream.getInputStream()).thenReturn(input);
        when(fileItemStream.getFieldName()).thenReturn("file");

        when(checksumItemStream.getFieldName()).thenReturn("checkSum");
        when(checksumItemStream.getInputStream()).thenReturn(new ByteArrayInputStream("12345".getBytes()));

        when(upload.getItemIterator(httpRequest)).thenReturn(itemIterator);
        when(checkSumSupport.convertMessageDigestToHex(any())).thenReturn("1234");
        when(checkSumSupport.createSha256MessageDigest()).thenReturn(MessageDigest.getInstance("SHA-256"));

        /* execute + test (checksum failure) */
        assertThrowsExceptionContainingMessage(BadRequestException.class, "Binaries checksum check failed",
                () -> serviceToTest.uploadBinaries(PROJECT1, randomUuid, httpRequest));

        /* test */
        verify(storage).store(eq("binaries.tar"), any(InputStream.class), any(Long.class)); // file was stored
        assertUploadEvent();

    }

    private void assertNoUploadEvent() {
        verifyNoInteractions(domainMessageService);
    }

    private void assertUploadEvent() {
        ArgumentCaptor<DomainMessage> captor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(domainMessageService).sendAsynchron(captor.capture());

        DomainMessage message = captor.getValue();
        assertNotNull(message);
        assertEquals(MessageID.BINARY_UPLOAD_DONE, message.getMessageId());
    }
}
