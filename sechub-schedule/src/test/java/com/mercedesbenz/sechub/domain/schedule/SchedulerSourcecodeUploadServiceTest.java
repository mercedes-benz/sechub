// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.util.ChecksumSHA256Service;
import com.mercedesbenz.sechub.sharedkernel.util.ZipSupport;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

public class SchedulerSourcecodeUploadServiceTest {

    private static final String PROJECT1 = "project1";
    private SchedulerSourcecodeUploadService serviceToTest;
    private ChecksumSHA256Service mockedChecksumService;
    private StorageService mockedStorageService;
    private UUID randomUuid;
    private ScheduleAssertService mockedAssertService;
    private MultipartFile file;

    private JobStorage storage;
    private ZipSupport mockedZipSupport;

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
        mockedZipSupport = mock(ZipSupport.class);

        /* attach at service to test */
        serviceToTest = new SchedulerSourcecodeUploadService();
        serviceToTest.checksumSHA256Service = mockedChecksumService;
        serviceToTest.storageService = mockedStorageService;
        serviceToTest.assertService = mockedAssertService;
        serviceToTest.zipSupport = mockedZipSupport;
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
        serviceToTest.assertion = mock(UserInputAssertion.class);
        serviceToTest.auditLogService = mock(AuditLogService.class);

    }

    @Test
    void without_spring_container_zip_validation_is_enabled() {
        assertTrue(serviceToTest.validateZip);
    }

    @Test
    void without_spring_container_checksum_validation_is_enabled() {
        assertTrue(serviceToTest.validateChecksum);
    }

    @Test
    void when_checksum_correct_and_is_zip__correct_no_failure() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedZipSupport.isZipFile(any())).thenReturn(true);

        /* execute */
        serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
    }

    @Test
    void when_checksum_is_NOT_correct_but_valid_zipfile_throws_404() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(false);
        when(mockedZipSupport.isZipFile(any())).thenReturn(true);

        /* execute + test */
        assertThrows(NotAcceptableException.class, () -> serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum"));
    }

    @Test
    void when_checksum_is_NOT_correct_but_valid_zipfile_but_checksum_validation_is_disabled_no_failure() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(false);
        when(mockedZipSupport.isZipFile(any())).thenReturn(true);

        serviceToTest.validateChecksum = false;

        /* execute + test (no exception) */
        serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
    }

    @Test
    void when_checksum_is_correct_but_NOT_valid_zipfile_but_zip_validation_is_disabled_no_failure() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedZipSupport.isZipFile(any())).thenReturn(false);

        serviceToTest.validateZip = false;

        /* execute + test (no exception) */
        serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
    }

    @Test
    void when_checksum_is_correct_but_not_valid_zipfile_throws_404() {
        /* prepare */
        when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(true);
        when(mockedZipSupport.isZipFile(any())).thenReturn(false);

        /* execute + test */
        assertThrows(NotAcceptableException.class, () -> serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum"));
    }

}
