package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.storage.core.JobStorage;

@Service
public class PrepareWrapperFileUploadService {
    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperFileUploadService.class);

    @Autowired
    PrepareWrapperStorageService storageService;

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
        JobStorage jobStorage = storageService.getJobStorage(projectId, jobUUID);

        UploadContext uploadContext;

        if (file.getName().endsWith(".tar")) {
            uploadContext = new UploadContext(FILENAME_BINARIES_TAR, FILENAME_BINARIES_TAR_FILESIZE, FILENAME_BINARIES_TAR_CHECKSUM);
        } else if (file.getName().endsWith(".zip")) {
            uploadContext = new UploadContext(FILENAME_SOURCECODE_ZIP, FILENAME_SOURCECODE_ZIP_FILESIZE, FILENAME_SOURCECODE_ZIP_CHECKSUM);
        } else {
            throw new IllegalArgumentException("File must be a zip or tar file");
        }

        upload(file, checkSum, jobStorage, uploadContext);
    }

    private void upload(File file, String checkSum, JobStorage jobStorage, UploadContext uploadContext) {
        try (InputStream inputStream = new FileInputStream(file)) {
            long fileSize = file.length();

            long checksumSizeInBytes = checkSum.getBytes().length;

            String fileSizeAsString = "" + fileSize;
            long fileSizeAsStringSizeInBytes = fileSizeAsString.getBytes().length;

            jobStorage.store(uploadContext.filename, inputStream, fileSize);
            jobStorage.store(uploadContext.filesize, new StringInputStream(fileSizeAsString), fileSizeAsStringSizeInBytes);
            jobStorage.store(uploadContext.checksum, new StringInputStream(checkSum), checksumSizeInBytes);

        } catch (IOException e) {
            LOG.error("Was not able to store file: " + uploadContext.filename, e);
            throw new RuntimeException("Was not able to upload sources");
        }
    }

    private static class UploadContext {
        String filename;
        String filesize;
        String checksum;

        public UploadContext(String filename, String filename_filesize, String filename_checksum) {
            this.filename = filename;
            this.filesize = filename_filesize;
            this.checksum = filename_checksum;
        }
    }
}
