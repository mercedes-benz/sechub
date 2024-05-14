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
public class PrepareWrapperSourceUploadService {

    // See SchedulerSourcecodeuploadService.java
    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperSourceUploadService.class);
    private static final long EMPTY_ZIP_FILE_SIZE = 22;

    @Autowired
    PrepareWrapperStorageService storageService;

    // TODO: 14.05.24 laura must be tested

    public void uploadSourceCode(String projectId, UUID jobUUID, File file, String checkSum) {
        // TODO: 14.05.24 laura traceid
        assertSourceUploadParams(projectId, jobUUID, file, checkSum);
        storeUploadFileAndSha256Checksum(projectId, jobUUID, file, checkSum);
        LOG.info("uploaded sourcecode");
    }

    private void assertSourceUploadParams(String projectId, UUID jobUUID, File file, String checkSum) {
        // TODO: 14.05.24 laura
    }

    private void storeUploadFileAndSha256Checksum(String projectId, UUID jobUUID, File file, String checkSum) {
        JobStorage jobStorage = storageService.getJobStorage(projectId, jobUUID);

        try (InputStream inputStream = new FileInputStream(file)) {
            long fileSize = file.length();

            if (fileSize <= EMPTY_ZIP_FILE_SIZE) {
                throw new IOException("Uploaded sourcecode zip file may not be empty!");
            }

            long checksumSizeInBytes = checkSum.getBytes().length;

            String fileSizeAsString = "" + fileSize;
            long fileSizeAsStringSizeInBytes = fileSizeAsString.getBytes().length;

            jobStorage.store(FILENAME_SOURCECODE_ZIP, inputStream, fileSize);

            jobStorage.store(FILENAME_SOURCECODE_ZIP_FILESIZE, new StringInputStream(fileSizeAsString), fileSizeAsStringSizeInBytes);
            jobStorage.store(FILENAME_SOURCECODE_ZIP_CHECKSUM, new StringInputStream(checkSum), checksumSizeInBytes);

        } catch (IOException e) {
            LOG.error("Was not able to store zipped sources! ", e);
            throw new RuntimeException("Was not able to upload sources");
        }
    }
}
