package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.*;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.storage.core.JobStorage;

@Service
public class PrepareWrapperBinaryUploadService {
    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperBinaryUploadService.class);

    @Autowired
    PrepareWrapperStorageService storageService;

    // TODO: 14.05.24 laura must be tested
    public void uploadBinaries(String projectId, UUID jobUUID, File file, String checkSum) {
        // TODO: 14.05.24 laura traceid
        assertBinaryUploadParams(projectId, jobUUID, file, checkSum);
        storeUploadFileAndSha256Checksum(projectId, jobUUID, file, checkSum);
        LOG.info("uploaded sourcecode");
    }

    private void assertBinaryUploadParams(String projectId, UUID jobUUID, File file, String checkSum) {

        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("projectId may not be empty.");
        }

        if (jobUUID == null) {
            throw new IllegalArgumentException("jobUUID may not be null.");
        }

        if (file == null) {
            throw new IllegalArgumentException("file may not be null.");
        }

        if (checkSum == null || checkSum.isEmpty()) {
            throw new IllegalArgumentException("checkSum may not be empty.");
        }
    }

    private void storeUploadFileAndSha256Checksum(String projectId, UUID jobUUID, File file, String checkSum) {
        JobStorage jobStorage = storageService.getJobStorage(projectId, jobUUID);

        try (InputStream inputStream = new FileInputStream(file)) {
            long fileSize = file.length();

            if (fileSize == 0) {
                throw new IOException("Uploaded binaries tar file may not be empty!");
            }

            String fileSizeAsString = "" + fileSize;
            long fileSizeAsStringSizeInBytes = fileSizeAsString.getBytes().length;

            jobStorage.store(FILENAME_BINARIES_TAR_CHECKSUM, new StringInputStream(checkSum), checkSum.getBytes().length);
            jobStorage.store(FILENAME_BINARIES_TAR, inputStream, fileSize);
            jobStorage.store(FILENAME_BINARIES_TAR_FILESIZE, new StringInputStream(fileSizeAsString), fileSizeAsStringSizeInBytes);

        } catch (IOException e) {
            throw new RuntimeException("Was not able to upload binaries");
        }
    }
}
