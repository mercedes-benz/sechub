// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.util.ZipSupport;


public class JobStorage {

	private ZipSupport zipSupport = ZipSupport.INSTANCE;
	
	private static final Logger LOG = LoggerFactory.getLogger(JobStorage.class);
	private String projectId;
	private UUID jobUUID;
	private Path path;
	
	public JobStorage(Path rootLocation, String projectId, UUID jobUUID) {
		notNull(rootLocation, "rootlocation may not be null");
		notNull(projectId, "projectId may not be null");
		notNull(jobUUID, "jobUUID may not be null");
		
		this.projectId=projectId;
		this.jobUUID=jobUUID;
		
		this.path = rootLocation.resolve(projectId).resolve(jobUUID.toString());
	}
	

	public void store(String fileName, MultipartFile file) {
		notNull(fileName, "fileName may not be null!");
		if (fileName.contains("..")) {
			// This is a security check
			throw new StorageException(
					"Cannot store file with relative path outside current directory " + fileName);
		}
			
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file " + fileName);
			}

			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				throw new StorageException("Could not initialize storage directory at:" + path, e);
			}
			String originFileName = StringUtils.cleanPath(file.getOriginalFilename());
			LOG.info("job:{}: storing {} as {} in project {}", jobUUID, originFileName, fileName, projectId);


			try (InputStream inputStream = file.getInputStream()) {
				Path pathToFile = getPathToFile(fileName);
				Files.copy(inputStream, pathToFile, StandardCopyOption.REPLACE_EXISTING);
				LOG.debug("Stored:{} at {}", fileName, pathToFile);
			} catch(ResourceAccessException e){
				LOG.debug("Uploaded file exceeds maximum file size limit." + e);
				throw new NotAcceptableException("Provided file exceeds file limit.");
			}
		} catch (IOException e) {
			throw new StorageException("Failed to store file " + fileName, e);
		}
	}

	private Path getPathToFile(String fileName) {
		notNull(fileName, "fileName may not be null!");
		return this.path.resolve(fileName);
	}

	public Path load(String filename) {
		return path.resolve(filename);
	}

	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException("Could not read file: " + filename);

			}
		} catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	public void deleteAll() {
		if (! path.toFile().exists()) {
			return;
		}
		try {
			FileSystemUtils.deleteRecursively(path);
			LOG.info("deleted all inside {}",path);
		} catch (IOException e) {
			throw new StorageException("Was not able to delete job storage",e);
		}
	}

	@Override
	public String toString() {
		return "JobStorage [projectId=" + projectId + ", jobUUID=" + jobUUID
				+ ", path=" + path + "]";
	}
	/**
	 * Checks if the file inside the store (it must be uploaded ...) is a valid zip file
	 * @param fileName
	 * @return <code>true</code> when valid, <code>false</code> when not valid or not existing
	 */
	public boolean isValidZipFile(String fileName) {
		Path filePath = getPathToFile(fileName);
		return zipSupport.isZipFile(filePath);
	}


	public boolean isExisting(String fileName) {
		return getPathToFile(fileName).toFile().exists();
		
	}

	public String getAbsolutePath(String fileName) {
		return getPathToFile(fileName).toFile().getAbsolutePath();
	}
}
