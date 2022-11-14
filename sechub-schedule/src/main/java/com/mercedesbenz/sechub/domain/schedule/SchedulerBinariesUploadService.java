// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.input.MessageDigestCalculatingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.execute.UseCaseUserUploadsBinaries;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

@Service
@RolesAllowed(RoleConstants.ROLE_USER)
public class SchedulerBinariesUploadService {
    private static final String PARAMETER_FILE = "file";
    private static final String PARAMETER_CHECKSUM = "checkSum";
    private static final Logger LOG = LoggerFactory.getLogger(SchedulerBinariesUploadService.class);

    @Autowired
    SchedulerBinariesUploadConfiguration configuration;

    @Autowired
    StorageService storageService;

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

        } catch (SizeLimitExceededException sizeLimitExceededException) {

            LOG.error("Size limit reached: {}", sizeLimitExceededException.getMessage());
            throw new BadRequestException("Binaries upload maximum reached. Please reduce your upload size.", sizeLimitExceededException);

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
        /* prepare */
        long binaryFileSizeFromUser = getBinaryFileSize(request);

        String checksumFromUser = null;
        String checksumCalculated = null;

        boolean fileDefinedByUser = false;
        boolean checkSumDefinedByUser = false;

        long realContentLengthInBytes = -1;

        JobStorage jobStorage = storageService.getJobStorage(projectId, jobUUID);

        ServletFileUpload upload = servletFileUploadFactory.create();

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
        FileItemIterator iterStream = upload.getItemIterator(request);

        while (iterStream.hasNext()) {
            FileItemStream item = iterStream.next();
            String fieldName = item.getFieldName();
            switch (fieldName) {
            case PARAMETER_CHECKSUM:
                try (InputStream checkSumInputStream = item.openStream()) {
                    checksumFromUser = Streams.asString(checkSumInputStream);

                    assertion.assertIsValidSha256Checksum(checksumFromUser);

                    jobStorage.store(FILENAME_BINARIES_TAR_CHECKSUM, new StringInputStream(checksumFromUser), checksumFromUser.getBytes().length);
                    LOG.info("uploaded user defined checksum as file for {}", jobUUID);
                }
                checkSumDefinedByUser = true;
                break;
            case PARAMETER_FILE:
                try (InputStream fileInputstream = item.openStream()) {

                    MessageDigest digest = checkSumSupport.createSha256MessageDigest();

                    MessageDigestCalculatingInputStream messageDigestInputStream = new MessageDigestCalculatingInputStream(fileInputstream, digest);
                    CountingInputStream byteCountingInputStream = new CountingInputStream(messageDigestInputStream);

                    jobStorage.store(FILENAME_BINARIES_TAR, byteCountingInputStream, binaryFileSizeFromUser);
                    LOG.info("uploaded binaries for {}", jobUUID);

                    realContentLengthInBytes = byteCountingInputStream.getByteCount();

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

    private void assertMultipart(HttpServletRequest request) {
        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new BadRequestException("The binary upload request did not contain multipart content");
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
