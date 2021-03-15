// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.util.StringInputStream;
import com.daimler.sechub.commons.model.SecHubRuntimeException;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsSourceCode;
import com.daimler.sechub.sharedkernel.util.FileChecksumSHA256Service;
import com.daimler.sechub.sharedkernel.util.ZipSupport;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import com.daimler.sechub.storage.core.JobStorage;

@Service
public class SchedulerUploadService {

	static final String SOURCECODE_ZIP = "sourcecode.zip";
	static final String SOURCECODE_ZIP_CHECKSUM = SOURCECODE_ZIP+".checksum";

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerUploadService.class);

	@Autowired
	StorageService storageService;

	@Autowired
	FileChecksumSHA256Service checksumSHA256Service;

	@Autowired
	ScheduleAssertService assertService;

	@Autowired
	ZipSupport zipSupport;

	@Autowired
	LogSanitizer logSanitizer;

	@Autowired
	AuditLogService auditLogService;

	@Autowired
	UserInputAssertion assertion;
	
	@UseCaseUserUploadsSourceCode(@Step(number = 2, name = "Try to find project and upload sourcecode as zipfile", description = "When project is found and user has access and job is initializing the sourcecode file will be uploaded"))
	public void uploadSourceCode(String projectId, UUID jobUUID, MultipartFile file, String checkSum) {
		assertion.isValidProjectId(projectId);
		assertion.isValidJobUUID(jobUUID);
		notNull(file, "file may not be null!");

		String traceLogID = logSanitizer.sanitize(UUIDTraceLogID.traceLogID(jobUUID),-1);

		auditLogService.log("Wants to upload source code to project {}, {}", logSanitizer.sanitize(projectId, 30) ,traceLogID);

		assertService.assertUserHasAccessToProject(projectId);

		assertJobFoundAndStillInitializing(projectId, jobUUID);

		JobStorage jobStorage = storageService.getJobStorage(projectId, jobUUID);
		Path tmpFile = null;
		try {
			/* prepare a tmp file for validation */
			try {
				tmpFile = Files.createTempFile("sechub_schedule_upload_tmp", null);
				file.transferTo(tmpFile);
			} catch (IOException e) {
				LOG.error("Was not able to create temp file of zipped sources!", e);
				throw new SecHubRuntimeException("Was not able to create temp file");
			}
			/* validate */
			assertValidZipFile(tmpFile);
			assertCheckSumCorrect(checkSum, tmpFile);

			/* now store */
			try {
				jobStorage.store(SOURCECODE_ZIP, file.getInputStream());
				// we also store new checksum - so not necessary to calculate at adapters again!
				jobStorage.store(SOURCECODE_ZIP_CHECKSUM, new StringInputStream(checkSum));
			} catch (IOException e) {
				LOG.error("Was not able to store zipped sources! {}", traceLogID, e);
				throw new SecHubRuntimeException("Was not able to upload sources");
			}
			LOG.info("uploaded sourcecode for {}", traceLogID);
		} finally {
			if (tmpFile != null && Files.exists(tmpFile)) {
				try {
					Files.delete(tmpFile);
				} catch (IOException e) {
					LOG.error("Was not able delete former temp file for zipped sources! {}",traceLogID, e);
				}
			}
		}

	}

	private void assertCheckSumCorrect(String checkSum, Path path) {
		if (!checksumSHA256Service.hasCorrectChecksum(checkSum, path.toAbsolutePath().toString())) {
			LOG.error("uploaded file is has not correct checksum! So something happend on upload!");
			throw new NotAcceptableException("Sourcecode checksum check failed");
		}
	}

	private void assertValidZipFile(Path path) {
		if (!zipSupport.isZipFile(path)) {
			LOG.error("uploaded file is NOT a valid ZIP file! Doing garbage control!");
			throw new NotAcceptableException("Sourcecode is not wrapped inside a valid zip file");
		}
	}

	private void assertJobFoundAndStillInitializing(String projectId, UUID jobUUID) {
		ScheduleSecHubJob secHubJob = assertService.assertJob(projectId, jobUUID);
		ExecutionState state = secHubJob.getExecutionState();
		if (!ExecutionState.INITIALIZING.equals(state)) {
			throw new NotAcceptableException("Not in correct state");// upload only possible when in initializing state
		}
	}

}