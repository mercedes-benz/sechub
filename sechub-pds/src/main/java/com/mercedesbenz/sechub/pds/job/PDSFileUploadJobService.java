// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.input.MessageDigestInputStream;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport.CheckSumValidationResult;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.PDSBadRequestException;
import com.mercedesbenz.sechub.pds.UploadSizeConfiguration;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.storage.PDSMultiStorageService;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserUploadsJobData;
import com.mercedesbenz.sechub.pds.util.PDSArchiveSupportProvider;
import com.mercedesbenz.sechub.storage.core.JobStorage;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;

@Service
@RolesAllowed({ PDSRoleConstants.ROLE_SUPERADMIN, PDSRoleConstants.ROLE_USER })
public class PDSFileUploadJobService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSFileUploadJobService.class);

    private static final int MAX_FILENAME_LENGTH = 40;

    @Autowired
    CheckSumSupport checksumSupport;

    @Autowired
    PDSWorkspaceService workspaceService;

    @Autowired
    PDSMultiStorageService storageService;

    @Autowired
    PDSArchiveSupportProvider archiveSupportProvider;

    @Autowired
    PDSJobRepository repository;

    @Autowired
    UploadSizeConfiguration configuration;

    @Autowired
    PDSLogSanitizer PDSLogSanitizer;

    @Autowired
    PDSServletFileUploadFactory servletFileUploadFactory;

    @UseCaseUserUploadsJobData(@PDSStep(name = "service call", description = "uploaded file is stored by storage service", number = 2))
    public void upload(UUID jobUUID, String fileName, HttpServletRequest request) {
        notNull(jobUUID, "job uuid may not be null");
        validateFileName(fileName);

        assertMultipart(request);

        PDSJob job = assertJobFound(jobUUID, repository);
        assertJobIsInState(job, PDSJobStatusState.CREATED);

        try {
            handleUploadAndProblems(jobUUID, request, fileName);
        } catch (Exception e) {
            LOG.error("Was not able to upload file: {} for job: {}.", fileName, jobUUID, e);
            throw e;
        }
        LOG.info("Upload has been done for PDS job: {}", jobUUID);

    }

    private void handleUploadAndProblems(UUID jobUUID, HttpServletRequest request, String fileName) {
        try {

            startUpload(jobUUID, request, fileName);

        } catch (FileUploadSizeException fileUploadSizeException) {

            LOG.error("Size limit reached: {}", fileUploadSizeException.getMessage());
            throw new PDSBadRequestException("Upload maximum reached. Please reduce your upload size.", fileUploadSizeException);

        } catch (SizeLimitExceededException sizeLimitExceededException) {

            LOG.error("Size limit reached: {}", sizeLimitExceededException.getMessage());
            throw new PDSBadRequestException("Upload maximum reached. Please reduce your upload size.", sizeLimitExceededException);

        } catch (UnsupportedEncodingException e) {

            throw new IllegalStateException("Encoding not support - should never happen", e);

        } catch (FileUploadException e) {

            throw new PDSBadRequestException("The given multipart content is not accepted.", e);

        } catch (IOException e) {
            throw new SecHubRuntimeException("Was not able to upload job data because of IO problems.", e);
        }
    }

    private void startUpload(UUID jobUUID, HttpServletRequest request, String fileName) throws FileUploadException, IOException, UnsupportedEncodingException {
        /* prepare */
        LOG.debug("Start upload file: {} for PDS job: {}", fileName, jobUUID);

        Long fileSizeFromUser = getFileSize(request);

        String checksumFromUser = null;
        String checksumCalculated = null;

        boolean fileDefinedByUser = false;
        boolean checkSumDefinedByUser = false;

        long realContentLengthInBytes = -1;

        JobStorage jobStorage = storageService.getJobStorage(jobUUID);

        JakartaServletFileUpload<?, ?> upload = servletFileUploadFactory.create();

        long maxUploadSize = configuration.getMaxUploadSizeInBytes();
        long maxUploadSizeWithHeaders = maxUploadSize + 600; // we accept 600 bytes more for header, checksum etc.

        if (fileSizeFromUser != null && fileSizeFromUser > maxUploadSizeWithHeaders) {
            throw new PDSBadRequestException("The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " exceeds the allowed upload size.");
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
            case MULTIPART_CHECKSUM:
                try (InputStream checkSumInputStream = item.getInputStream()) {
                    checksumFromUser = streamToString(checkSumInputStream);

                    CheckSumValidationResult validationResult = checksumSupport.validateSha256Checksum(checksumFromUser);
                    if (!validationResult.isValid()) {
                        throw new PDSBadRequestException(validationResult.getMessage());
                    }

                    jobStorage.store(fileName + DOT_CHECKSUM, new StringInputStream(checksumFromUser), checksumFromUser.getBytes().length);
                    LOG.info("uploaded user defined checksum as file for file: {} in PDS job: {}", fileName, jobUUID);
                }
                checkSumDefinedByUser = true;
                break;
            case MULTIPART_FILE:
                try (InputStream fileInputstream = item.getInputStream()) {

                    MessageDigest digest = checksumSupport.createSha256MessageDigest();

                    /* @formatter:off */
					MessageDigestInputStream messageDigestInputStream = MessageDigestInputStream.builder().
							                                              setInputStream(fileInputstream).
							                                              setMessageDigest(digest).
							                                              get();
					/* @formatter:on */

                    CountingInputStream byteCountingInputStream = new CountingInputStream(messageDigestInputStream);

                    if (fileSizeFromUser == null) {
                        jobStorage.store(fileName, byteCountingInputStream);
                    } else {
                        jobStorage.store(fileName, byteCountingInputStream, fileSizeFromUser);
                    }

                    LOG.info("uploaded file:{} for job:{}", fileName, jobUUID);

                    realContentLengthInBytes = byteCountingInputStream.getByteCount();
                    checksumCalculated = checksumSupport.convertMessageDigestToHex(digest);
                }
                fileDefinedByUser = true;
                break;
            default:
                LOG.warn("Given field '{}' is not supported while uploading job data to project {}, {}", PDSLogSanitizer.sanitize(fieldName, 30), jobUUID);
            }
        }

        if (!fileDefinedByUser) {
            throw new PDSBadRequestException("No file defined by user for job data upload!");
        }
        if (fileSizeFromUser != null && realContentLengthInBytes != fileSizeFromUser) {
            throw new PDSBadRequestException("The real file size was not equal to the user provided file size length.");
        }
        if (!checkSumDefinedByUser) {
            throw new PDSBadRequestException("No checksum defined by user for job data upload!");
        }
        if (checksumFromUser == null) {
            throw new PDSBadRequestException("No user checksum available for job data upload!");
        }
        if (checksumCalculated == null) {
            throw new PDSBadRequestException("Upload was not possible!");
        }
        assertCheckSumCorrect(checksumFromUser, checksumCalculated);
    }

    private Long getFileSize(HttpServletRequest request) {
        Long fileSizeFromUser = null;

        String fileSizeFromUserField = request.getHeader(FILE_SIZE_HEADER_FIELD_NAME);

        LOG.debug("File size from user field: {}", fileSizeFromUserField);

        if (fileSizeFromUserField != null) {
            try {
                fileSizeFromUser = Long.valueOf(fileSizeFromUserField);
            } catch (NumberFormatException ex) {
                throw new PDSBadRequestException("The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " is not formatted as a number.");
            }

            if (fileSizeFromUser < 0) {
                throw new PDSBadRequestException("The file size in header field " + FILE_SIZE_HEADER_FIELD_NAME + " cannot be negative.");
            }
        }

        return fileSizeFromUser;
    }

    private void assertMultipart(HttpServletRequest request) {
        if (!JakartaServletFileUpload.isMultipartContent(request)) {
            throw new PDSBadRequestException("The upload request did not contain multipart content");
        }
    }

    private void assertCheckSumCorrect(String checkSumFromUser, String checksumCalculated) {
        if (!Objects.equals(checkSumFromUser, checksumCalculated)) {
            LOG.error("Uploaded file has incorrect sha256 checksum! Something must have happened during the upload.");
            throw new PDSBadRequestException("Binaries checksum check failed");
        }
    }

    /* sanity check to avoid path traversal etc. */
    private void validateFileName(String fileName) {
        notNull(fileName, "filename may not be null!");
        if (fileName.length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("filename exceeds maximum length of " + MAX_FILENAME_LENGTH + " chars");
        }
        for (char c : fileName.toCharArray()) {
            boolean accepted = Character.isDigit(c) || Character.isAlphabetic(c);
            accepted = accepted || c == '-' || c == '_' || c == '.';
            if (!accepted) {
                throw new IllegalArgumentException(
                        "filename contains illegal characters. Allowed is only [a-zA-Z\\.-_] maximum length of " + MAX_FILENAME_LENGTH + " chars");
            }
        }
    }

    String streamToString(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
