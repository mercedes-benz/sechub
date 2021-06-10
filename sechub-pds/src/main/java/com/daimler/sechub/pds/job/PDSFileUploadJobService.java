// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.util.StringInputStream;
import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.pds.storage.PDSMultiStorageService;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseUserUploadsJobData;
import com.daimler.sechub.pds.util.PDSFileChecksumSHA256Service;
import com.daimler.sechub.pds.util.PDSZipSupport;
import com.daimler.sechub.storage.core.JobStorage;

@Service
@RolesAllowed({ PDSRoleConstants.ROLE_SUPERADMIN, PDSRoleConstants.ROLE_USER })
public class PDSFileUploadJobService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSFileUploadJobService.class);

    private static final int MAX_FILENAME_LENGTH = 40;

    @Autowired
    PDSFileChecksumSHA256Service checksumService;

    @Autowired
    PDSWorkspaceService workspaceService;

    @Autowired
    PDSMultiStorageService storageService;

    @Autowired
    PDSZipSupport zipSupport;

    @Autowired
    PDSJobRepository repository;

    @UseCaseUserUploadsJobData(@PDSStep(name = "service call", description = "uploaded file is stored by storage service", number = 2))
    public void upload(UUID jobUUID, String fileName, MultipartFile file, String checkSum) {
        notNull(jobUUID, "job uuid may not be null");
        notNull(file, "file may not be null");
        notNull(checkSum, "checkSum may not be null");
        validateFileName(fileName);

        PDSJob job = assertJobFound(jobUUID, repository);
        assertJobIsInState(job, PDSJobStatusState.CREATED);

        /*
         * fetch job storage without path - storage service decides location
         * automatically
         */
        JobStorage storage = storageService.getJobStorage(jobUUID);
        Path tmpFile = null;
        try {
            /* prepare a tmp file for validation */
            try {
                tmpFile = Files.createTempFile("pds_upload_tmp", null);
                file.transferTo(tmpFile);
            } catch (IOException e) {
                LOG.error("Was not able to create temp file of zipped sources!", e);
                throw new IllegalStateException("Was not able to create temp file");
            }
            /* validate */
            if (fileName.toLowerCase().endsWith(".zip")) {
                // we check for ZIP file correctness, so automated unzipping can be done
                // correctly
                assertValidZipFile(tmpFile);
            }
            assertCheckSumCorrect(checkSum, tmpFile);
            
            /* now store */
            try {
                LOG.info("Upload file {} for job {} to storage", fileName, jobUUID);
                storage.store(fileName, file.getInputStream());

                // we also store checksum
                storage.store(fileName + ".checksum", new StringInputStream(checkSum));

            } catch (IOException e) {
                LOG.error("Was not able to store {} for job {}, reason:", fileName, jobUUID, e.getMessage());
                throw new IllegalArgumentException("Cannot store given file", e);
            }

        } finally {
            if (tmpFile != null && Files.exists(tmpFile)) {
                try {
                    Files.delete(tmpFile);
                } catch (IOException e) {
                    LOG.error("Was not able delete former temp file for zipped sources! {}",jobUUID, e);
                }
            }
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

    private void assertCheckSumCorrect(String checkSum, Path path) {
        if (!checksumService.hasCorrectChecksum(checkSum, path.toAbsolutePath().toString())) {
            LOG.error("uploaded file has not correct checksum! Something must have happened during the upload!");
            throw new PDSNotAcceptableException("Sourcecode checksum check failed");
        }
    }

    private void assertValidZipFile(Path path) {
        if (!zipSupport.isZipFile(path)) {
            Path fileName = path.getFileName();

            LOG.error("uploaded file {} is NOT a valid ZIP file!", fileName);
            throw new PDSNotAcceptableException(fileName + " is not a valid zip file");
        }
    }

}
