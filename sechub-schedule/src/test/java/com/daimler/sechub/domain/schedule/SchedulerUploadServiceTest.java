// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.storage.JobStorage;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.sharedkernel.util.FileChecksumSHA256Service;

public class SchedulerUploadServiceTest {
	
	private static final String PROJECT1 = "project1";
	private SchedulerUploadService serviceToTest;
	private FileChecksumSHA256Service mockedChecksumService;
	private StorageService mockedStorageService;
	private UUID randomUuid;
	private ScheduleAssertService mockedAssertService;
	private MultipartFile file;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private JobStorage storage;
	
	@Before
	public void before() {
		randomUuid = UUID.randomUUID();
		
		mockedChecksumService=mock(FileChecksumSHA256Service.class);
		mockedStorageService=mock(StorageService.class);
		mockedAssertService=mock(ScheduleAssertService.class);
		
		ScheduleSecHubJob job = new ScheduleSecHubJob();
		when(mockedAssertService.assertJob(PROJECT1, randomUuid)).thenReturn(job);
		storage = mock(JobStorage.class);
		when(storage.getAbsolutePath(SchedulerUploadService.SOURCECODE_ZIP)).thenReturn("the-path");
		when(mockedStorageService.getJobStorage(PROJECT1, randomUuid)).thenReturn(storage);
		
		file = mock(MultipartFile.class);

		/* attach at service to test */
		serviceToTest = new SchedulerUploadService();
		serviceToTest.checksumSHA256Service=mockedChecksumService;
		serviceToTest.storageService=mockedStorageService;
		serviceToTest.assertService=mockedAssertService;
		
	}
	
	@Test
	public void when_checksum_correct_and_is_zip__correct_no_failure() {
		/* prepare */
		when(mockedChecksumService.hasCorrectChecksum("mychecksum", "the-path")).thenReturn(true);
		when(storage.isValidZipFile(SchedulerUploadService.SOURCECODE_ZIP)).thenReturn(true);
		
		/* execute */
		serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
	}
	
	@Test
	public void when_checksum_is_NOT_correct_but_valid_zipfile_throws_404() {
		/* prepare */
		when(mockedChecksumService.hasCorrectChecksum("mychecksum", "the-path")).thenReturn(false);
		when(storage.isValidZipFile(SchedulerUploadService.SOURCECODE_ZIP)).thenReturn(true);
		expectedException.expect(NotAcceptableException.class);
		
		/* execute */
		serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
	}
	
	@Test
	public void when_checksum_is_correct_but_not_valid_zipfile_throws_404() {
		/* prepare */
		when(mockedChecksumService.hasCorrectChecksum("mychecksum", "the-path")).thenReturn(true);
		when(storage.isValidZipFile(SchedulerUploadService.SOURCECODE_ZIP)).thenReturn(false);
		expectedException.expect(NotAcceptableException.class);
		
		/* execute */
		serviceToTest.uploadSourceCode(PROJECT1, randomUuid, file, "mychecksum");
	}

}
