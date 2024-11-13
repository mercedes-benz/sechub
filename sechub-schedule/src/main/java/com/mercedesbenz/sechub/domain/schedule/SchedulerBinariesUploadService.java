// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILENAME_BINARIES_TAR;
import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILENAME_BINARIES_TAR_CHECKSUM;
import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILENAME_BINARIES_TAR_FILESIZE;
import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILE_SIZE_HEADER_FIELD_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.input.MessageDigestInputStream;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.StorageMessageData;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.storage.SecHubStorageService;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsBinaries;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.JobStorage;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;

@Service
@RolesAllowed(RoleConstants.ROLE_USER)
public class SchedulerBinariesUploadService {
    private static final String PARAMETER_FILE = "file";
    private static final String PARAMETER_CHECKSUM = "checkSum";
    private static final Logger LOG = LoggerFactory.getLogger(SchedulerBinariesUploadService.class);

    @Autowired
    SchedulerBinariesUploadConfiguration configuration;

    @Autowired
    SecHubStorageService storageService;

    @Autowired
    CheckSumSupport checkSumSupport;

    @Autowired
    ScheduleAssertService assertService;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    UserInputAssertion assertion;

    @Autowired
    ServletFileUploadFactory servletFileUploadFactory;

    @Autowired
    DomainMessageService domainMessageService;

    @UseCaseUserUploadsBinaries(@Step(number = 2, name = "Try to find project and upload binaries as tar", description = "When project is found and user has access and job is initializing the binaries file will be uploaded"))
    public void uploadBinaries(String projectId, UUID jobUUID, HttpServletRequest request) {
        /* assert */
        assertion.assertIsValidProjectId(projectId);
        assertion.assertIsValidJobUUID(jobUUID);

        auditLogService.log("Wants to upload binaries to project {}, {}", logSanitizer.sanitize(projectId, 30), jobUUID);

        assertService.assertUserHasAccessToProject(projectId);
        assertService.assertProjectAllowsWriteAccess(projectId);

        assertJobFoundAndStillInitializing(projectId, jobUUID);

        assertMultipart(request);

        /* execute upload */
        handleUploadAndProblems(projectId, jobUUID, request);

    }

    private void handleUploadAndProblems(String projectId, UUID jobUUID, HttpServletRequest request) {
        try {

            startUpload(projectId, jobUUID, request);

        } catch (FileUploadSizeException fileUploadSizeException) {

            LOG.error("Size limit reached: {}", fileUploadSizeException.getMessage());
            throw new BadRequestException("Binaries upload maximum reached. Please reduce your upload size.", fileUploadSizeException);

        } catch (FileSizeLimitExceededException fileSizeLimitExceededException) {

            LOG.error("Size limit reached: {}", fileSizeLimitExceededException.getMessage());
            throw new BadRequestException("Binaries upload maximum reached. Please reduce your upload file size.", fileSizeLimitExceededException);

        } catch (UnsupportedEncodingException e) {

            throw new IllegalStateException("Encoding not support - should never happen", e);

        } catch (FileUploadException e) {

            throw new BadRequestException("The given multipart content is not accepted.", e);

        } catch (IOException e) {
            throw new SecHubRuntimeException("Was not able to upload binaries because of IO problems.", e);
        }
    }

    private void startUpload(String projectId, UUID jobUUID, HttpServletRequest request) throws FileUploadException, IOException, UnsupportedEncodingException {
        JobStorage jobStorage = storageService.createJobStorageForProject(projectId, jobUUID);
        try {
            store(projectId, jobUUID, request, jobStorage);
        } finally {
            jobStorage.close();
        }
    }

    private void store(String projectId, UUID jobUUID, HttpServletRequest request, JobStorage jobStorage)
            throws FileUploadException, IOException, UnsupportedEncodingException {
        /* prepare */
        long binaryFileSizeFromUser = getBinaryFileSize(request);

        String checksumFromUser = null;
        String checksumCalculated = null;

        boolean fileDefinedByUser = false;
        boolean checkSumDefinedByUser = false;

        long realContentLengthInBytes = -1;

        JakartaServletFileUpload<?, ?> upload = servletFileUploadFactory.create();

        long maxUploadSize = configuration.getMaxUploadSizeInBytes();
        long maxUploadSizeWithHeaders = maxUploadSize + 600; // we accept 600 bytes more for header, checksum etc.

        if (binaryFileSizeFromUser > maxUploadSizeWithHeaders) {
            throw new BadRequestException("The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " exceeds the allowed upload size.");
        }

        upload.setSizeMax(maxUploadSizeWithHeaders);
        upload.setFileSizeMax(maxUploadSize);

        /*
         * Important: this next call of "upload.getItemIterator(..)" looks very simple,
         * but it creates a new <code>FileItemIteratorImpl</code> instances which
         * internally does some heavy things on creation: It does create a new input
         * stream, checks for max size handling and much more. We want to avoid creating
         * the iterator multiple times!
         *
         * Also any access to the origin request to access the parameter/field names
         * does always trigger a multipart resolving which uses again the underlying
         * standard Servlet mechanism and the configured max sizes there!
         *
         * So we could only check parameters with another item iterator when we want to
         * handle this specialized, but the item iterator should be created only one
         * time (see explained reason before).
         *
         * This is the reason, why we do not check the user input at the beginning but
         * only at the end. This is maybe inconvenient for the user when forgetting to
         * define a field, but this normally happens only one time and the benefit of
         * avoiding side effects. In addition, the performance (speed) does matter here.
         *
         * ------------------------- So please do NOT change! -------------------------
         */
        FileItemInputIterator iterStream = upload.getItemIterator(request);

        while (iterStream.hasNext()) {
            FileItemInput item = iterStream.next();
            String fieldName = item.getFieldName();
            switch (fieldName) {
            case PARAMETER_CHECKSUM:
                try (InputStream checkSumInputStream = item.getInputStream()) {
                    checksumFromUser = streamToString(checkSumInputStream);

                    assertion.assertIsValidSha256Checksum(checksumFromUser);

                    jobStorage.store(FILENAME_BINARIES_TAR_CHECKSUM, new StringInputStream(checksumFromUser), checksumFromUser.getBytes().length);
                    LOG.info("uploaded user defined checksum as file for {}", jobUUID);
                }
                checkSumDefinedByUser = true;
                break;
            case PARAMETER_FILE:
                try (InputStream fileInputstream = item.getInputStream()) {

                    MessageDigest digest = checkSumSupport.createSha256MessageDigest();

                    /* @formatter:off */
					MessageDigestInputStream messageDigestInputStream = MessageDigestInputStream.builder().
							                                              setInputStream(fileInputstream).
							                                              setMessageDigest(digest).
							                                              get();
					/* @formatter:on */

                    CountingInputStream byteCountingInputStream = new CountingInputStream(messageDigestInputStream);

                    jobStorage.store(FILENAME_BINARIES_TAR, byteCountingInputStream, binaryFileSizeFromUser);
                    LOG.info("uploaded binaries for {}", jobUUID);

                    realContentLengthInBytes = byteCountingInputStream.getByteCount();

                    // We send here the event that the upload has been done, even
                    // when the following checksum validation would fail. Reason: we want to measure
                    // the traffic
                    // even when somebody does always define the wrong checksum again and again...
                    sendBinaryUploadDoneEvent(projectId, jobUUID, realContentLengthInBytes);

                    /* upload file size information to storage */
                    String fileSizeAsString = "" + realContentLengthInBytes;
                    long fileSizeAsStringSizeInBytes = fileSizeAsString.getBytes().length;
                    jobStorage.store(FILENAME_BINARIES_TAR_FILESIZE, new StringInputStream(fileSizeAsString), fileSizeAsStringSizeInBytes);

                    checksumCalculated = checkSumSupport.convertMessageDigestToHex(digest);
                }
                fileDefinedByUser = true;
                break;
            default:
                LOG.warn("Given field '{}' is not supported while uploading binaries to project {}, {}", logSanitizer.sanitize(fieldName, 30),
                        logSanitizer.sanitize(projectId, 30), jobUUID);
            }
        }

        if (!fileDefinedByUser) {
            throw new BadRequestException("No file defined by user for binaries upload!");
        }
        if (realContentLengthInBytes != binaryFileSizeFromUser) {
            throw new BadRequestException("The real file size was not equal to the user provided file size length.");
        }
        if (!checkSumDefinedByUser) {
            throw new BadRequestException("No checksum defined by user for binaries upload!");
        }
        if (checksumFromUser == null) {
            throw new BadRequestException("No user checksum available for binaries upload!");
        }
        if (checksumCalculated == null) {
            throw new BadRequestException("Upload of binaries was not possible!");
        }
        assertCheckSumCorrect(checksumFromUser, checksumCalculated);
    }

    @IsSendingAsyncMessage(MessageID.BINARY_UPLOAD_DONE)
    private void sendBinaryUploadDoneEvent(String projectId, UUID jobUUID, long fileSizeAsStringSizeInBytes) {
        DomainMessage message = new DomainMessage(MessageID.BINARY_UPLOAD_DONE);

        StorageMessageData storageDataMessage = new StorageMessageData();
        storageDataMessage.setJobUUID(jobUUID);
        storageDataMessage.setProjectId(projectId);
        storageDataMessage.setSince(LocalDateTime.now());
        storageDataMessage.setSizeInBytes(fileSizeAsStringSizeInBytes);

        message.set(MessageDataKeys.SECHUB_JOB_UUID, jobUUID);
        message.set(MessageDataKeys.UPLOAD_STORAGE_DATA, storageDataMessage);

        domainMessageService.sendAsynchron(message);
    }

    private long getBinaryFileSize(HttpServletRequest request) {
        long binaryFileSizeFromUser = -1;

        String binaryFileSizeFromUserField = request.getHeader(FILE_SIZE_HEADER_FIELD_NAME);

        if (binaryFileSizeFromUserField == null) {
            throw new BadRequestException("Header field " + FILE_SIZE_HEADER_FIELD_NAME + " not set.");
        }

        try {
            binaryFileSizeFromUser = Long.valueOf(binaryFileSizeFromUserField);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " is not formatted as a number.");
        }

        if (binaryFileSizeFromUser < 0) {
            throw new BadRequestException("The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " cannot be negative.");
        }

        return binaryFileSizeFromUser;
    }

    String streamToString(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private void assertMultipart(HttpServletRequest request) {
        if (!JakartaServletFileUpload.isMultipartContent(request)) {
            throw new BadRequestException("The upload request did not contain multipart content");
        }
    }

    private void assertCheckSumCorrect(String checkSumFromUser, String checksumCalculated) {
        if (!Objects.equals(checkSumFromUser, checksumCalculated)) {
            LOG.error("Uploaded binary file has incorrect sha256 checksum! Something must have happened during the upload.");
            throw new BadRequestException("Binaries checksum check failed");
        }
    }

    private void assertJobFoundAndStillInitializing(String projectId, UUID jobUUID) {
        ScheduleSecHubJob secHubJob = assertService.assertJob(projectId, jobUUID);
        ExecutionState state = secHubJob.getExecutionState();
        if (!ExecutionState.INITIALIZING.equals(state)) {
            throw new BadRequestException("Not in correct state");// upload only possible when in initializing state
        }
    }

}
