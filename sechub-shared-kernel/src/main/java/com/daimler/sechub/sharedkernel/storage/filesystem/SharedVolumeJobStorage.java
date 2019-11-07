// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage.filesystem;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.ResourceAccessException;

import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.storage.JobStorage;
import com.daimler.sechub.sharedkernel.storage.StorageException;

public class SharedVolumeJobStorage implements JobStorage {

	private static final Logger LOG = LoggerFactory.getLogger(SharedVolumeJobStorage.class);
	private String projectId;
	private UUID jobUUID;
	private Path path;

	public SharedVolumeJobStorage(Path rootLocation, String projectId, UUID jobUUID) {
		notNull(rootLocation, "rootlocation may not be null");
		notNull(projectId, "projectId may not be null");
		notNull(jobUUID, "jobUUID may not be null");

		this.projectId = projectId;
		this.jobUUID = jobUUID;

		this.path = rootLocation.resolve(projectId).resolve(jobUUID.toString());
	}

	@Override
	public InputStream fetch(String name) throws IOException {
		Path path = getPathToFile(name);
		if (path == null) {
			return null;
		}
		return new FileInputStream(path.toFile());
	}

	@Override
	public void store(String name, InputStream stream) throws IOException {
		notNull(name, "name may not be null!");
		notNull(stream, "stream may not be null!");

		if (name.contains("..")) {
			// This is a security check
			throw new StorageException("Cannot store file with relative path outside current directory " + name);
		}

		if (stream == null) {
			throw new StorageException("Failed to store empty file " + name);
		}

		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new StorageException("Could not initialize storage directory at:" + path, e);
		}
		LOG.info("job:{}: storing {} in project {}", jobUUID, name, projectId);

		try (InputStream inputStream = stream) {
			Path pathToFile = getPathToFile(name);
			Files.copy(inputStream, pathToFile, StandardCopyOption.REPLACE_EXISTING);
			LOG.debug("Stored:{} at {}", name, pathToFile);
		} catch (ResourceAccessException e) {
			LOG.debug("Uploaded file exceeds maximum file size limit." + e);
			throw new NotAcceptableException("Provided file exceeds file limit.");
		}
	}

	private Path getPathToFile(String fileName) {
		notNull(fileName, "fileName may not be null!");
		return this.path.resolve(fileName);
	}

	public void deleteAll() throws IOException {
		if (!path.toFile().exists()) {
			return;
		}
		FileSystemUtils.deleteRecursively(path);
		LOG.info("deleted all inside {}", path);
	}

	@Override
	public String toString() {
		return "SharedVolumeJobStorage [projectId=" + projectId + ", jobUUID=" + jobUUID + ", path=" + path + "]";
	}

	public boolean isExisting(String fileName) {
		return getPathToFile(fileName).toFile().exists();

	}

}
