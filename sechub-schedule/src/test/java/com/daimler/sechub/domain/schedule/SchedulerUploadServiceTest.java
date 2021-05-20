// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.util.FileChecksumSHA256Service;
import com.daimler.sechub.sharedkernel.util.ZipSupport;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import com.daimler.sechub.storage.core.JobStorage;
import com.daimler.sechub.storage.core.StorageService;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class SchedulerUploadServiceTest {

	private static final String PROJECT1 = "project1";
	private SchedulerUploadService serviceToTest;
	private FileChecksumSHA256Service mockedChecksumService;
	private StorageService mockedStorageService;
	private UUID randomUuid;
	private ScheduleAssertService mockedAssertService;
	private MultipartFile file;

	@Rule
	public ExpectedException expectedException = ExpectedExceptionFactory.none();
	private JobStorage storage;
	private ZipSupport mockedZipSupport;

	@Before
	public void before() {
		randomUuid = UUID.randomUUID();

		mockedChecksumService=mock(FileChecksumSHA256Service.class);
		mockedStorageService=mock(StorageService.class);
		mockedAssertService=mock(ScheduleAssertService.class);

		ScheduleSecHubJob job = new ScheduleSecHubJob();
		when(mockedAssertService.assertJob(PROJECT1, randomUuid)).thenReturn(job);
		storage = mock(JobStorage.class);
		when(mockedStorageService.getJobStorage(PROJECT1, randomUuid)).thenReturn(storage);

		file = mock(MultipartFile.class);
		mockedZipSupport = mock(ZipSupport.class);

		/* attach at service to test */
		serviceToTest = new SchedulerUploadService();
		serviceToTest.checksumSHA256Service=mockedChecksumService;
		serviceToTest.storageService=mockedStorageService;
		serviceToTest.assertService=mockedAssertService;
		serviceToTest.zipSupport=mockedZipSupport;
		serviceToTest.logSanitizer=mock(LogSanitizer.class);
		serviceToTest.assertion=mock(UserInputAssertion.class);
		serviceToTest.auditLogService=mock(AuditLogService.class);

	}

	@Test
	public void when_checksum_correct_and_is_zip__correct_no_failure() {
		/* prepare */
		when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(true);
		when(mockedZipSupport.isZipFile(any())).thenReturn(true);

		/* execute */
		serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
	}

	@Test
	public void when_checksum_is_NOT_correct_but_valid_zipfile_throws_404() {
		/* prepare */
		when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(false);
		when(mockedZipSupport.isZipFile(any())).thenReturn(true);
		expectedException.expect(NotAcceptableException.class);

		/* execute */
		serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
	}

	@Test
	public void when_checksum_is_correct_but_not_valid_zipfile_throws_404() {
		/* prepare */
		when(mockedChecksumService.hasCorrectChecksum(eq("mychecksum"), any())).thenReturn(true);
		when(mockedZipSupport.isZipFile(any())).thenReturn(false);
		expectedException.expect(NotAcceptableException.class);

		/* execute */
		serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
	}

}
