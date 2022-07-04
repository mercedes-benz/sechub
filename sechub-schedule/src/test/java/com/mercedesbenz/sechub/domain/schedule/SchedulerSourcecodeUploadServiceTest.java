// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.util.ArchiveSupportProvider;
import com.mercedesbenz.sechub.sharedkernel.util.ChecksumSHA256Service;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

@SuppressWarnings("deprecation")
public class SchedulerSourcecodeUploadServiceTest {

    private static final String PROJECT1 = "project1";
    private SchedulerSourcecodeUploadService serviceToTest;
    private ChecksumSHA256Service mockedChecksumService;
    private StorageService mockedStorageService;
    private UUID randomUuid;
    private ScheduleAssertService mockedAssertService;
    private MultipartFile file;

    private JobStorage storage;
    private ArchiveSupport mockedArchiveSupport;
    private ArchiveSupportProvider archiveSupportProvider;
    private SchedulerSourcecodeUploadConfiguration configuration;

    @BeforeEach
    void beforeEach() {
        randomUuid = UUID.randomUUID();

        mockedChecksumService = mock(ChecksumSHA256Service.class);
        mockedStorageService = mock(StorageService.class);
        mockedAssertService = mock(ScheduleAssertService.class);

        ScheduleSecHubJob job = new ScheduleSecHubJob();
        when(mockedAssertService.assertJob(PROJECT1, randomUuid)).thenReturn(job);
        storage = mock(JobStorage.class);
        when(mockedStorageService.getJobStorage(PROJECT1, randomUuid)).thenReturn(storage);

        file = mock(MultipartFile.class);
        archiveSupportProvider = mock(ArchiveSupportProvider.class);
        mockedArchiveSupport = mock(ArchiveSupport.class);
        when(archiveSupportProvider.getArchiveSupport()).thenReturn(mockedArchiveSupport);

        configuration = mock(SchedulerSourcecodeUploadConfiguration.class);

        /* attach at service to test */
        serviceToTest = new SchedulerSourcecodeUploadService();
        serviceToTest.checksumSHA256Service = mockedChecksumService;
        serviceToTest.storageService = mockedStorageService;
        serviceToTest.assertService = mockedAssertService;
        serviceToTest.archiveSupportProvider = archiveSupportProvider;
        serviceToTest.configuration = configuration;

        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.assertion = mock(UserInputAssertion.class);
        serviceToTest.auditLogService = mock(AuditLogService.class);

    }

    @Test
    void when_checksum_correct_and_is_zip__correct_no_failure() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(true);

        when(configuration.isChecksumValidationEnabled()).thenReturn(true);
        when(configuration.isZipValidationEnabled()).thenReturn(true);

        /* execute */
        serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
    }

    @Test
    void when_checksum_is_NOT_correct_but_valid_zipfile_throws_404() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(false);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(true);

        when(configuration.isChecksumValidationEnabled()).thenReturn(true);
        when(configuration.isZipValidationEnabled()).thenReturn(false);

        /* execute + test */
        assertThrows(NotAcceptableException.class, () -> serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum"));
    }

    @Test
    void when_checksum_is_NOT_correct_but_valid_zipfile_but_checksum_validation_is_disabled_no_failure() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(false);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(true);

        when(configuration.isChecksumValidationEnabled()).thenReturn(false);
        when(configuration.isZipValidationEnabled()).thenReturn(true);

        /* execute + test (no exception) */
        serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
    }

    @Test
    void when_checksum_is_correct_but_NOT_valid_zipfile_but_zip_validation_is_disabled_no_failure() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(false);

        when(configuration.isChecksumValidationEnabled()).thenReturn(true);
        when(configuration.isZipValidationEnabled()).thenReturn(false);

        /* execute + test (no exception) */
        serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
    }

    @Test
    void when_checksum_is_correct_but_not_valid_zipfile_throws_404() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedArchiveSupport.isZipFileStream(any())).thenReturn(false);

        when(configuration.isChecksumValidationEnabled()).thenReturn(true);
        when(configuration.isZipValidationEnabled()).thenReturn(true);

        /* execute + test */
        assertThrows(NotAcceptableException.class, () -> serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum"));
    }

}
