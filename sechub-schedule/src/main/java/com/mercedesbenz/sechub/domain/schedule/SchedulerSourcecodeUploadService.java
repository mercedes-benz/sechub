// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.StorageMessageData;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsSourceCode;
import com.mercedesbenz.sechub.sharedkernel.util.ArchiveSupportProvider;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_USER)
public class SchedulerSourcecodeUploadService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerSourcecodeUploadService.class);

    /**
     * A constant for the size of an empty zip file - see
     * https://en.wikipedia.org/wiki/ZIP_(file_format)#Limits
     */
    private static final long EMPTY_ZIP_FILE_SIZE = 22;

    @Autowired
    SchedulerSourcecodeUploadConfiguration configuration;

    @Autowired
    StorageService storageService;

    @Autowired
    CheckSumSupport checkSumSupport;

    @Autowired
    ScheduleAssertService assertService;

    @Autowired
    ArchiveSupportProvider archiveSupportProvider;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    DomainMessageService domainMessageService;

    @UseCaseUserUploadsSourceCode(@Step(number = 2, name = "Try to find project and upload sourcecode as zipfile", description = "When project is found and user has access and job is initializing the sourcecode file will be uploaded"))
    public void uploadSourceCode(String projectId, UUID jobUUID, MultipartFile file, String checkSum) {
        /* assert */
        assertion.assertIsValidProjectId(projectId);
        assertion.assertIsValidJobUUID(jobUUID);

        assertion.assertIsValidSha256Checksum(checkSum);

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
            long fileSize = file.getSize();

            if (fileSize <= EMPTY_ZIP_FILE_SIZE) {
                throw new BadRequestException("Uploaded sourcecode zip file may not be empty!");
            }

            long checksumSizeInBytes = checkSum.getBytes().length;

            String fileSizeAsString = "" + fileSize;
            long fileSizeAsStringSizeInBytes = fileSizeAsString.getBytes().length;

            jobStorage.store(FILENAME_SOURCECODE_ZIP, inputStream, fileSize);
            sendSourceSourceUploadDoneEvent(projectId, jobUUID, fileSize);

            // we store the file size information inside storage - so we can use this for
            // PDS uploads when no reuse of storage is wanted.
            jobStorage.store(FILENAME_SOURCECODE_ZIP_FILESIZE, new StringInputStream(fileSizeAsString), fileSizeAsStringSizeInBytes);
            // we also store given checksum - so can be reused by security product
            jobStorage.store(FILENAME_SOURCECODE_ZIP_CHECKSUM, new StringInputStream(checkSum), checksumSizeInBytes);

        } catch (IOException e) {
            LOG.error("Was not able to store zipped sources! {}", traceLogID, e);
            throw new SecHubRuntimeException("Was not able to upload sources");
        }
    }

    @IsSendingAsyncMessage(MessageID.SOURCE_UPLOAD_DONE)
    private void sendSourceSourceUploadDoneEvent(String projectId, UUID jobUUID, long fileSizeInBytes) {
        DomainMessage message = new DomainMessage(MessageID.SOURCE_UPLOAD_DONE);

        StorageMessageData storageDataMessage = new StorageMessageData();
        storageDataMessage.setJobUUID(jobUUID);
        storageDataMessage.setProjectId(projectId);
        storageDataMessage.setSince(LocalDateTime.now());
        storageDataMessage.setSizeInBytes(fileSizeInBytes);

        message.set(MessageDataKeys.SECHUB_JOB_UUID, jobUUID);
        message.set(MessageDataKeys.UPLOAD_STORAGE_DATA, storageDataMessage);

        domainMessageService.sendAsynchron(message);
    }

    private void handleChecksumValidation(MultipartFile file, String checkSum, String traceLogID) {
        if (!configuration.isChecksumValidationEnabled()) {
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
        if (!configuration.isZipValidationEnabled()) {
            return;
        }
        try (InputStream inputStream = file.getInputStream()) {
            /* validate */
            assertValidZipFile(inputStream);

        } catch (IOException e) {
            LOG.error("Was not able to validate uploaded zip file", traceLogID, e);
            throw new SecHubRuntimeException("Was not able to validate uploaded ZIP sources");
        }
    }

    private void assertCheckSumCorrect(String checkSum, InputStream inputStream) {
        if (!checkSumSupport.hasCorrectSha256Checksum(checkSum, inputStream)) {
            LOG.error("Uploaded file has incorrect sha256 checksum! Something must have happened during the upload.");
            throw new NotAcceptableException("Sourcecode checksum check failed");
        }
    }

    @SuppressWarnings("deprecation")
    private void assertValidZipFile(InputStream inputStream) {
        if (!archiveSupportProvider.getArchiveSupport().isZipFileStream(inputStream)) {
            LOG.error("Uploaded file is NOT a valid ZIP file!");
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