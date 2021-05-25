// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.sharevolume.spring;

import static java.util.Objects.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.ResourceAccessException;

import com.daimler.sechub.storage.core.JobStorage;
import com.daimler.sechub.storage.core.StorageException;

public class SharedVolumeJobStorage implements JobStorage {

    private static final Logger LOG = LoggerFactory.getLogger(SharedVolumeJobStorage.class);
    private String storagePath;
    private UUID jobUUID;
    private Path volumePath;

    public SharedVolumeJobStorage(Path rootLocation, String storagePath, UUID jobUUID) {
        requireNonNull(rootLocation, "rootLocation may not be null");
        requireNonNull(storagePath, "storagePath may not be null");
        requireNonNull(jobUUID, "jobUUID may not be null");

        this.storagePath = storagePath;
        this.jobUUID = jobUUID;

        this.volumePath = rootLocation.resolve(storagePath).resolve(jobUUID.toString());
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
        requireNonNull(name, "name may not be null!");
        requireNonNull(stream, "stream may not be null!");

        if (name.contains("..")) {
            // This is a security check
            throw new StorageException("Cannot store file with relative path outside current directory " + name);
        }

        try {
            Files.createDirectories(volumePath);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage directory at:" + volumePath, e);
        }
        LOG.info("job:{}: storing {} in path {}", jobUUID, name, storagePath);

        Path pathToFile = getPathToFile(name);
        
        try (InputStream inputStream = stream) {
            Files.copy(inputStream, pathToFile, StandardCopyOption.REPLACE_EXISTING);
            
            LOG.debug("Stored:{} at {}", name, pathToFile);
        } catch (ResourceAccessException e) {
            throw new IOException("Was not able to store input stream into file "+ pathToFile, e);
        }
    }

    private Path getPathToFile(String fileName) {
        requireNonNull(fileName, "fileName may not be null!");
        return this.volumePath.resolve(fileName);
    }

    public void deleteAll() throws IOException {
        if (Files.notExists(volumePath)) {
            return;
        }
        FileSystemUtils.deleteRecursively(volumePath);
        LOG.info("deleted all inside {}", volumePath);
    }

    @Override
    public String toString() {
        return "SharedVolumeJobStorage [projectId=" + storagePath + ", jobUUID=" + jobUUID + ", path=" + volumePath + "]";
    }

    public boolean isExisting(String fileName) {
        return getPathToFile(fileName).toFile().exists();

    }

    @Override
    public Set<String> listNames() throws IOException {
        Set<String> names = new LinkedHashSet<>();
        if (Files.notExists(volumePath)) {
            Files.createDirectories(volumePath);
        }
        Files.list(volumePath).forEach(child -> names.add(child.getFileName().toString()));

        return names;
    }

}
