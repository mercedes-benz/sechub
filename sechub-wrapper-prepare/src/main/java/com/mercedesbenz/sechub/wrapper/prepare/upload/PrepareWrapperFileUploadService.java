// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static com.mercedesbenz.sechub.wrapper.prepare.upload.UploadExceptionExitCode.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.commons.core.util.SecHubStorageUtil;
import com.mercedesbenz.sechub.storage.core.JobStorage;

@Service
public class PrepareWrapperFileUploadService {
    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperFileUploadService.class);

    private final PrepareWrapperStorageService storageService;

    public PrepareWrapperFileUploadService(PrepareWrapperStorageService storageService) {
        this.storageService = storageService;
    }

    public void uploadFile(String projectId, UUID jobUUID, File file, String checkSum) {
        assertUploadParams(projectId, jobUUID, file, checkSum);
        storeUploadFileAndSha256Checksum(projectId, jobUUID, file, checkSum);
    }

    private void assertUploadParams(String projectId, UUID jobUUID, File file, String checkSum) {

        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("projectId may not be null or empty.");
        }

        if (jobUUID == null) {
            throw new IllegalArgumentException("jobUUID may not be null.");
        }

        if (file == null) {
            throw new IllegalArgumentException("file may not be null.");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("Upload file does not exist.");
        }

        if (checkSum == null || checkSum.isEmpty()) {
            throw new IllegalArgumentException("checkSum may not be empty.");
        }
    }

    private void storeUploadFileAndSha256Checksum(String projectId, UUID jobUUID, File file, String checkSum) {
        String storagePath = SecHubStorageUtil.createStoragePathForProject(projectId);

        JobStorage jobStorage = storageService.createJobStorageForPath(storagePath, jobUUID);

        try {
            UploadFileNameData uploadFileNameData;

            if (file.getName().endsWith(".tar")) {
                uploadFileNameData = new UploadFileNameData(FILENAME_BINARIES_TAR, FILENAME_BINARIES_TAR_FILESIZE, FILENAME_BINARIES_TAR_CHECKSUM);
            } else if (file.getName().endsWith(".zip")) {
                uploadFileNameData = new UploadFileNameData(FILENAME_SOURCECODE_ZIP, FILENAME_SOURCECODE_ZIP_FILESIZE, FILENAME_SOURCECODE_ZIP_CHECKSUM);
            } else {
                throw new IllegalArgumentException("File must be a zip or tar file");
            }

            upload(file, checkSum, jobStorage, uploadFileNameData);
        } finally {
            jobStorage.close();

        }
    }

    private void upload(File file, String checkSum, JobStorage jobStorage, UploadFileNameData uploadFileNameData) {
        try (InputStream inputStream = new FileInputStream(file)) {
            long fileSize = file.length();

            long checksumSizeInBytes = checkSum.getBytes().length;

            String fileSizeAsString = "" + fileSize;
            long fileSizeAsStringSizeInBytes = fileSizeAsString.getBytes().length;

            jobStorage.store(uploadFileNameData.fileFilename, inputStream, fileSize);
            jobStorage.store(uploadFileNameData.filesizeFilename, new StringInputStream(fileSizeAsString), fileSizeAsStringSizeInBytes);
            jobStorage.store(uploadFileNameData.checksumFilename, new StringInputStream(checkSum), checksumSizeInBytes);

        } catch (IOException e) {
            LOG.error("Was not able to store files: {}, {} and {}", uploadFileNameData.fileFilename, uploadFileNameData.checksumFilename,
                    uploadFileNameData.filesizeFilename, e);
            throw new PrepareWrapperUploadException("Was not able to upload data to the storage!", UNABLE_TO_STORE_FILE);
        }
    }

    private static class UploadFileNameData {
        String fileFilename;
        String filesizeFilename;
        String checksumFilename;

        public UploadFileNameData(String fileFilename, String filesizeFilename, String checksumFilename) {
            this.fileFilename = fileFilename;
            this.filesizeFilename = filesizeFilename;
            this.checksumFilename = checksumFilename;
        }
    }
}
