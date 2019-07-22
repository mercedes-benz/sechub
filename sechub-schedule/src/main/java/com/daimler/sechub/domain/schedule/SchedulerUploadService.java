// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.storage.JobStorage;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsSourceCode;
import com.daimler.sechub.sharedkernel.util.FileChecksumSHA256Service;

@Service
public class SchedulerUploadService {

	static final String SOURCECODE_ZIP = "sourcecode.zip";

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerUploadService.class);

	@Autowired
	StorageService storageService;
	
	@Autowired
	FileChecksumSHA256Service checksumSHA256Service;
	
	@Autowired
	ScheduleAssertService assertService;

	@UseCaseUserUploadsSourceCode(@Step(number = 2, name = "Try to find project annd upload sourcecode as zipfile", description = "When project is found and user has access and job is initializing the sourcecode file will be uploaded"))
	public void uploadSourceCode(String projectId, UUID jobUUID, MultipartFile file, String checkSum) {
		notEmpty(projectId, "Project id may not be empty!");
		notNull(jobUUID, "jobUUID may not be null!");
		notNull(file, "file may not be null!");

		assertService.assertUserHasAccessToProject(projectId);

		assertJobFoundAndStillInitializing(projectId, jobUUID);
		
		JobStorage jobStorage = storageService.getJobStorage(projectId, jobUUID);
		jobStorage.store(SOURCECODE_ZIP, file);
		
		assertValidZipFile(jobStorage);
		assertCheckSumCorrect(checkSum, jobStorage);
		
		LOG.info("uploaded sourcecode for job {}", jobUUID);
	}

	private void assertCheckSumCorrect(String checkSum, JobStorage jobStorage) {
		if (! checksumSHA256Service.hasCorrectChecksum(checkSum, jobStorage.getAbsolutePath(SOURCECODE_ZIP))) {
			LOG.error("uploaded file is has not correct checksum! So something happend on upload!");
			jobStorage.deleteAll();
			throw new NotAcceptableException("Sourcecode checksum check failed");
		}
	}

	private void assertValidZipFile(JobStorage jobStorage) {
		if (! jobStorage.isValidZipFile(SOURCECODE_ZIP)) {
			LOG.error("uploaded file is NOT a valid ZIP file! Doing garbage control!");
			jobStorage.deleteAll();
			throw new NotAcceptableException("Sourcecode is not wrapped inside a valid zip file");
		}
	}

	private void assertJobFoundAndStillInitializing(String projectId, UUID jobUUID) {
		ScheduleSecHubJob secHubJob = assertService.assertJob(projectId, jobUUID);
		ExecutionState state = secHubJob.getExecutionState();
		if (! ExecutionState.INITIALIZING.equals(state)) {
			throw new NotAcceptableException("Not in correct state");// upload only possible when in initializing state
		}
	}

}