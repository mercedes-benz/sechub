// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.sharevolume.spring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.storage.core.SharedVolumeSetup;
import com.mercedesbenz.sechub.storage.core.StorageException;

public abstract class AbstractSharedVolumePropertiesSetup implements SharedVolumeSetup {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSharedVolumePropertiesSetup.class);

    /**
     * @return properties upload directory path, or {@link null}!
     */
    protected abstract String getConfiguredUploadDirectory();

    private String uploadDir;

    public String getUploadDir() {
        if (uploadDir == null) {
            /* create lazy so only temp folders created for tests where really necessary */
            uploadDir = ensuredUploadDirectory();
            LOG.info("Upload directory set to:{}", uploadDir);
        }
        return uploadDir;
    }

    @Override
    public boolean isAvailable() {
        return !UNDEFINED.equals(getConfiguredUploadDirectory());
    }

    private String ensuredUploadDirectory() {
        String propertiesUploadDir = getConfiguredUploadDirectory();

        if (getConfiguredUploadDirectory() != null && !propertiesUploadDir.startsWith("temp")) {
            File file = new File(propertiesUploadDir);
            if (!file.exists()) {
                LOG.info("Upload directory {} did not exists, so start creating it", propertiesUploadDir);
                if (!file.mkdirs()) {
                    LOG.error("FATAL: was not able to create upload directory!");
                    throw new IllegalStateException("Was not able to create upload directory:" + propertiesUploadDir);
                }
            }
            LOG.info("Using {} as shared volume directory for uploads", propertiesUploadDir);
            return propertiesUploadDir;
        }
        /* at this point there is no upload directory defined - so start a fallback */
        try {
            LOG.info(
                    "Temp upload directory NOT set, so creating a new temp directory. This should be used only on testing or when you just use ONE server instance. Using multiple server instances you need an NFS.");

            Path rootPath = Files.createTempDirectory("sechub-fallback-sharedvolume");
            Path uploadPath = rootPath.resolve("upload");
            Files.createDirectories(uploadPath);
            uploadPath.toFile().deleteOnExit();

            return uploadPath.toFile().getAbsolutePath();

        } catch (IOException e) {
            throw new StorageException("Was not able to create temporary upload dir", e);
        }
    }

}