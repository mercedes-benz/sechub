// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.test.JUnitAssertionAddon.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.DelegatingServletInputStream;

import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

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

    @BeforeEach
    void beforeEach() {
        randomUuid = UUID.randomUUID();

        checkSumSupport = mock(CheckSumSupport.class);
        storageService = mock(StorageService.class);
        assertService = mock(ScheduleAssertService.class);
        storage = mock(JobStorage.class);
        httpRequest = mock(HttpServletRequest.class);
        configuration = mock(SchedulerBinariesUploadConfiguration.class);

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

    }

    @Test
    void when_no_multipart_in_http_request_a_bad_request_is_returned() {
        /* execute */
        assertThrowsExceptionContainingMessage(BadRequestException.class, "did not contain multipart",
                () -> serviceToTest.uploadBinaries(PROJECT1, randomUuid, httpRequest));

    }

    @Test
    void when_illegal_content_bad_request_returned() throws Exception {
        /* prepare */
        InputStream input = new ByteArrayInputStream("i-am-illegal-content-without boundary or multipart".getBytes());
        ServletInputStream inputStream = new DelegatingServletInputStream(input);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getContentType()).thenReturn("multipart/");
        when(httpRequest.getInputStream()).thenReturn(inputStream);

        /* execute */
        assertThrowsExceptionContainingMessage(BadRequestException.class, "multipart content is not accepted",
                () -> serviceToTest.uploadBinaries(PROJECT1, randomUuid, httpRequest));

    }
}
