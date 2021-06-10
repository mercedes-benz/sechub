// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.sharevolume.spring;

import static java.util.Objects.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.daimler.sechub.storage.core.JobStorage;
import com.daimler.sechub.storage.core.JobStorageFactory;
import com.daimler.sechub.storage.core.SharedVolumeSetup;

public class SharedVolumeJobStorageFactory implements JobStorageFactory{
	private Path sharedVolumeUploadDirectory;

	public SharedVolumeJobStorageFactory(SharedVolumeSetup sharedVolumeSetup) {
		requireNonNull(sharedVolumeSetup, "sharedVolumeSetup may not be null!");
		if (! sharedVolumeSetup.isAvailable()) {
			throw new IllegalStateException("Shared Volume setup not available!");
		}
		this.sharedVolumeUploadDirectory = sharedVolumeSetup.getUploadDir() != null ? Paths.get(sharedVolumeSetup.getUploadDir()) : null;
	}

	@Override
	public JobStorage createJobStorage(String projectId, UUID jobUUID) {
		return new SharedVolumeJobStorage(sharedVolumeUploadDirectory, projectId, jobUUID);
	}

}
