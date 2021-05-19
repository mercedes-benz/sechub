// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.job.PDSJobAssert.*;
import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.pds.usecase.PDSStep;
import com.daimler.sechub.pds.usecase.UseCaseUserUploadsJobData;
import com.daimler.sechub.pds.util.PDSFileChecksumSHA256Service;

@Service
@RolesAllowed({PDSRoleConstants.ROLE_SUPERADMIN, PDSRoleConstants.ROLE_USER})
public class PDSFileUploadJobService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSFileUploadJobService.class);

    private static final int MAX_FILENAME_LENGTH = 40;

    @Autowired
    PDSFileChecksumSHA256Service checksumService;
    
    @Autowired
    PDSWorkspaceService workspaceService;

    @Autowired
    PDSJobRepository repository;

    @UseCaseUserUploadsJobData(@PDSStep(name="service call",description = "uploaded file be stored in job workspace upload location. Also checksum is validated",number=2))
    public void upload(UUID jobUUID, String fileName, MultipartFile file, String checkSum) {
        notNull(jobUUID, "job uuid may not be null");
        notNull(file, "file may not be null");
        notNull(checkSum, "checkSum may not be null");
        validateFileName(fileName);

        PDSJob job = assertJobFound(jobUUID,repository);
        assertJobIsInState(job,PDSJobStatusState.CREATED);

        File jobFolder = workspaceService.getUploadFolder(jobUUID);
        File uploadFile = new File(jobFolder, fileName);

        try {
            LOG.info("Upload file {} for job {} to {}",fileName,jobUUID, uploadFile.getAbsolutePath());
            FileUtils.copyInputStreamToFile(file.getInputStream(), uploadFile);
        } catch (IOException e) {
            LOG.error("Was not able to store {} for job {}, reason:", fileName, jobUUID, e.getMessage());
            throw new IllegalArgumentException("Cannot store given file", e);
        }

        assertCheckSumCorrect(checkSum, uploadFile.toPath());
    }

    
    private void assertCheckSumCorrect(String checkSum, Path path) {
        if (!checksumService.hasCorrectChecksum(checkSum, path.toAbsolutePath().toString())) {
            LOG.error("uploaded file is has not correct checksum! So something happend on upload!");
            throw new PDSNotAcceptableException("Sourcecode checksum check failed");
        }
    }

    public void deleteAllUploads(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null");

        File jobFolder = workspaceService.getUploadFolder(jobUUID);
        try {
            FileUtils.deleteDirectory(jobFolder);
        } catch (IOException e) {
            LOG.error("Was not able to delete uploads for job {}, reason:", jobUUID, e.getMessage());
            throw new IllegalArgumentException("Cannot store given file", e);
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


}
