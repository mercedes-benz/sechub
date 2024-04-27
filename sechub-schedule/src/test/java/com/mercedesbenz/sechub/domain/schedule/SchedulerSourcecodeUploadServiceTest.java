// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.util.ArchiveSupportProvider;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

@SuppressWarnings("deprecation")
public class SchedulerSourcecodeUploadServiceTest {

    private static final String PROJECT1 = "project1";
    private SchedulerSourcecodeUploadService serviceToTest;
    private CheckSumSupport checkSumSupport;
    private StorageService mockedStorageService;
    private UUID randomUuid;
    private ScheduleAssertService mockedAssertService;
    private MultipartFile file;
    private String checkSum = "mychecksum";

    private JobStorage storage;
    private ArchiveSupport mockedArchiveSupport;
    private ArchiveSupportProvider archiveSupportProvider;
    private SchedulerSourcecodeUploadConfiguration configuration;
    private DomainMessageService domainMessageService;

    @BeforeEach
    void beforeEach() throws IOException {
        randomUuid = UUID.randomUUID();

        checkSumSupport = mock(CheckSumSupport.class);
        mockedStorageService = mock(StorageService.class);
        mockedAssertService = mock(ScheduleAssertService.class);

        ScheduleSecHubJob job = new ScheduleSecHubJob();
        when(mockedAssertService.assertJob(PROJECT1, randomUuid)).thenReturn(job);
        storage = mock(JobStorage.class);
        when(mockedStorageService.getJobStorage(PROJECT1, randomUuid)).thenReturn(storage);

        file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(1024L); // just not empty

        archiveSupportProvider = mock(ArchiveSupportProvider.class);
        mockedArchiveSupport = mock(ArchiveSupport.class);
        when(archiveSupportProvider.getArchiveSupport()).thenReturn(mockedArchiveSupport);

        configuration = mock(SchedulerSourcecodeUploadConfiguration.class);
        domainMessageService = mock(DomainMessageService.class);

        /* attach at service to test */
        serviceToTest = new SchedulerSourcecodeUploadService();
        serviceToTest.checkSumSupport = checkSumSupport;
        serviceToTest.storageService = mockedStorageService;
        serviceToTest.assertService = mockedAssertService;
        serviceToTest.archiveSupportProvider = archiveSupportProvider;
        serviceToTest.configuration = configuration;
        serviceToTest.domainMessageService = domainMessageService;

        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.assertion = mock(UserInputAssertion.class);
        serviceToTest.auditLogService = mock(AuditLogService.class);

    }

    @ParameterizedTest
    @ValueSource(longs = { 0, 10, 22 })
    void when_zipfile_is_empty_a_bad_request_400_is_thrown_even_when_zipfile_validation_is_disabled(long fileSize) {
        /* prepare */
        when(checkSumSupport.hasCorrectSha256Checksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(true);

        when(configuration.isChecksumValidationEnabled()).thenReturn(false);
        when(configuration.isZipValidationEnabled()).thenReturn(false);

        when(file.getSize()).thenReturn(fileSize);

        /* execute + test */
        assertThrows(BadRequestException.class, () -> serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, checkSum));

        /* test */
        assertNoUploadEvent();
    }

    @Test
    void when_checksum_correct_and_is_zip__correct_no_failure() {
        /* prepare */
        when(checkSumSupport.hasCorrectSha256Checksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(true);

        when(configuration.isChecksumValidationEnabled()).thenReturn(true);
        when(configuration.isZipValidationEnabled()).thenReturn(true);

        /* execute */
        serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, checkSum);
    }

    @Test
    void when_checksum_is_NOT_correct_but_valid_zipfile_throws_404() {
        /* prepare */
        when(checkSumSupport.hasCorrectSha256Checksum(eq("mychecksum"), any())).thenReturn(false);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(true);

        when(configuration.isChecksumValidationEnabled()).thenReturn(true);
        when(configuration.isZipValidationEnabled()).thenReturn(false);

        /* execute + test */
        assertThrows(NotAcceptableException.class, () -> serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, checkSum));

        /* test */
        assertNoUploadEvent();
    }

    @Test
    void when_checksum_is_NOT_correct_but_valid_zipfile_but_checksum_validation_is_disabled_no_failure() {
        /* prepare */
        when(checkSumSupport.hasCorrectSha256Checksum(eq("mychecksum"), any())).thenReturn(false);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(true);

        when(configuration.isChecksumValidationEnabled()).thenReturn(false);
        when(configuration.isZipValidationEnabled()).thenReturn(true);

        /* execute + test (no exception) */
        serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, checkSum);

        /* test */
        assertUploadEvent();
    }

    @Test
    void when_checksum_is_correct_but_NOT_valid_zipfile_but_zip_validation_is_disabled_no_failure() {
        /* prepare */
        when(checkSumSupport.hasCorrectSha256Checksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(false);

        when(configuration.isChecksumValidationEnabled()).thenReturn(true);
        when(configuration.isZipValidationEnabled()).thenReturn(false);

        /* execute + test (no exception) */
        serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, checkSum);

        /* test */
        assertUploadEvent();
    }

    @Test
    void when_checksum_is_correct_but_not_valid_zipfile_throws_404() {
        /* prepare */
        when(checkSumSupport.hasCorrectSha256Checksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(false);

        when(configuration.isChecksumValidationEnabled()).thenReturn(true);
        when(configuration.isZipValidationEnabled()).thenReturn(true);

        /* execute + test */
        assertThrows(NotAcceptableException.class, () -> serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, checkSum));
    }

    private void assertNoUploadEvent() {
        verifyNoInteractions(domainMessageService);
    }

    private void assertUploadEvent() {
        ArgumentCaptor<DomainMessage> captor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(domainMessageService).sendAsynchron(captor.capture());

        DomainMessage message = captor.getValue();
        assertNotNull(message);
        assertEquals(MessageID.SOURCE_UPLOAD_DONE, message.getMessageId());
    }
}
