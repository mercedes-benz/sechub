// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Component
public class SharedVolumePropertiesSetup implements SharedVolumeSetup {
	

	private static final Logger LOG = LoggerFactory.getLogger(SharedVolumePropertiesSetup.class);

	/**
     * Folder location for storing files. When using "temp" a temporary folder on server side will be used
     */
	@MustBeDocumented(value="Defines the root path for shared volume uploads - e.g. for sourcecode.zip etc.")
	@Value("${sechub.storage.sharedvolume.upload.dir}")// we set explicit NO default value here, so must be defined
    private String propertiesUploadDir;

	private String uploadDir;
	
    public String getUploadDir() {
		if (uploadDir==null) {
			/* create lazy so only temp folders created for tests where really necessary */
			uploadDir = createUploadDir();
			LOG.info("Upload directory set to:{}",uploadDir);
		}
		return uploadDir;
	}


	private String createUploadDir() {
		if (propertiesUploadDir!=null && ! propertiesUploadDir.startsWith("temp")) {
			return propertiesUploadDir;
		}
		try {
			Path rootPath = Files.createTempDirectory("sechub-integration-test");
			Path uploadPath = rootPath.resolve("upload");
			Files.createDirectories(uploadPath);
			uploadPath.toFile().deleteOnExit();
			LOG.info("Temporary upload dir for tests is at:{}",uploadPath);
			return uploadPath.toFile().getAbsolutePath();
			
		} catch (IOException e) {
			throw new StorageException("Was not able to create temporary upload dir", e);
		}
	}

}