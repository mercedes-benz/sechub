// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage.filesystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.SecHubRuntimeException;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.storage.core.SharedVolumeSetup;
import com.daimler.sechub.storage.core.StorageException;

@Component
public class SharedVolumePropertiesSetup implements SharedVolumeSetup {


	private static final Logger LOG = LoggerFactory.getLogger(SharedVolumePropertiesSetup.class);
	private static final String UNDEFINED_UPLOAD_DIR ="undefined";
	/**
     * Folder location for storing files. When using "temp" a temporary folder on server side will be used
     */
	@MustBeDocumented(value="Defines the root path for shared volume uploads - e.g. for sourcecode.zip etc. When using keyword *temp* as path, this will create a temporary directory (for testing).",scope="storage")
	@Value("${sechub.storage.sharedvolume.upload.dir:"+UNDEFINED_UPLOAD_DIR+"}") // we use undefined here. Will be used in #isValid()
    private String propertiesUploadDir;

	private String uploadDir;

    public String getUploadDir() {
		if (uploadDir==null) {
			/* create lazy so only temp folders created for tests where really necessary */
			uploadDir = ensuredUploadDirectory();
			LOG.info("Upload directory set to:{}",uploadDir);
		}
		return uploadDir;
	}

    @Override
    public boolean isAvailable() {
    	return ! UNDEFINED_UPLOAD_DIR.equals(propertiesUploadDir);
    }


	private String ensuredUploadDirectory() {
		if (propertiesUploadDir!=null && ! propertiesUploadDir.startsWith("temp")) {
			File file = new File(propertiesUploadDir);
			if (!file.exists()) {
				LOG.info("Upload directory {} did not exists, so start creating it",propertiesUploadDir);
				if (!file.mkdirs()) {
					LOG.error("FATAL: was not able to create upload directory!");
					throw new SecHubRuntimeException("Was not able to create upload directory:"+propertiesUploadDir);
				}
			}
			LOG.info("Using {} as shared volume directory for uploads",propertiesUploadDir);
			return propertiesUploadDir;
		}
		try {
			LOG.info("Temp upload directy set, so creating a new temp directory. This should be used only on testing or when you just use ONE server instance. Using multiple server instances you need an NFS.");

			Path rootPath = Files.createTempDirectory("sechub-temp-sharedvolume");
			Path uploadPath = rootPath.resolve("upload");
			Files.createDirectories(uploadPath);
			uploadPath.toFile().deleteOnExit();

			LOG.info("Temporary upload dir is at:{}",uploadPath);

			return uploadPath.toFile().getAbsolutePath();

		} catch (IOException e) {
			throw new StorageException("Was not able to create temporary upload dir", e);
		}
	}

}