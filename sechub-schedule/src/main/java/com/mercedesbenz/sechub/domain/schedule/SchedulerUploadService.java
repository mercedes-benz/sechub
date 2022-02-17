// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsSourceCode;
import com.mercedesbenz.sechub.sharedkernel.util.FileChecksumSHA256Service;
import com.mercedesbenz.sechub.sharedkernel.util.ZipSupport;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

@Service
public class SchedulerUploadService {

    static final String SOURCECODE_ZIP = "sourcecode.zip";
    static final String SOURCECODE_ZIP_CHECKSUM = SOURCECODE_ZIP + ".checksum";

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerUploadService.class);

    @Value("${sechub.server.upload.validate.zip:true}")
    @MustBeDocumented(value = "With `false` ZIP validation on sechub server side is disabled. So ZIP validation must be done at delegated security products! You should disable the validation only for testing security product behaviours!")
    boolean validateZip = true;

    @MustBeDocumented(value = "With `false` checksum validation (sha256) on sechub server side is disabled. So sha256 validation must be done at delegated security products! You should disable the validation only for testing security product behaviours!")
    @Value("${sechub.server.upload.validate.checksum:true}")
    boolean validateChecksum = true;

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
        /* assert */
        assertion.isValidProjectId(projectId);
        assertion.isValidJobUUID(jobUUID);
        assertion.isValidSha256Checksum(checkSum);

        notNull(file, "file may not be null!");

        String traceLogID = logSanitizer.sanitize(UUIDTraceLogID.traceLogID(jobUUID), -1);

        auditLogService.log("Wants to upload source code to project {}, {}", logSanitizer.sanitize(projectId, 30), traceLogID);

        assertService.assertUserHasAccessToProject(projectId);
        assertService.assertProjectAllowsWriteAccess(projectId);

        assertJobFoundAndStillInitializing(projectId, jobUUID);

        handleZipValidation(file, traceLogID);
        handleChecksumValidation(file, checkSum, traceLogID);

        /* now store */
        storeUploadFileAndSha256Checksum(projectId, jobUUID, file, checkSum, traceLogID);
        LOG.info("uploaded sourcecode for {}", traceLogID);

    }

    private void storeUploadFileAndSha256Checksum(String projectId, UUID jobUUID, MultipartFile file, String checkSum, String traceLogID) {
        JobStorage jobStorage = storageService.getJobStorage(projectId, jobUUID);
        try (InputStream inputStream = file.getInputStream()) {
            jobStorage.store(SOURCECODE_ZIP, inputStream);
            // we also store new checksum - so not necessary to calculate at adapters again!
            jobStorage.store(SOURCECODE_ZIP_CHECKSUM, new StringInputStream(checkSum));
        } catch (IOException e) {
            LOG.error("Was not able to store zipped sources! {}", traceLogID, e);
            throw new SecHubRuntimeException("Was not able to upload sources");
        }
    }

    private void handleChecksumValidation(MultipartFile file, String checkSum, String traceLogID) {
        if (!validateChecksum) {
            return;
        }
        try (InputStream inputStream = file.getInputStream()) {
            /* validate */
            assertCheckSumCorrect(checkSum, inputStream);

        } catch (IOException e) {
            LOG.error("Was not able to validate uploaded file checksum", traceLogID, e);
            throw new SecHubRuntimeException("Was not able to validate uploaded sources checksum");
        }
    }

    private void handleZipValidation(MultipartFile file, String traceLogID) {
        if (!validateZip) {
            return;
        }
        try (InputStream inputStream = file.getInputStream()) {
            /* validate */
            assertValidZipFile(file.getInputStream());

        } catch (IOException e) {
            LOG.error("Was not able to validate uploaded zip file", traceLogID, e);
            throw new SecHubRuntimeException("Was not able to validate uploaded ZIP sources");
        }
    }

    private void assertCheckSumCorrect(String checkSum, InputStream inputStream) {
        if (!validateChecksum) {
            return;
        }
        if (!checksumSHA256Service.hasCorrectChecksum(checkSum, inputStream)) {
            LOG.error("uploaded file has not correct checksum! Something must have happened during the upload!");
            throw new NotAcceptableException("Sourcecode checksum check failed");
        }
    }

    private void assertValidZipFile(InputStream inputStream) {
        if (!zipSupport.isZipFile(inputStream)) {
            LOG.error("uploaded file is NOT a valid ZIP file!");
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