// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.sharevolume.spring;

import static java.util.Objects.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import com.mercedesbenz.sechub.storage.core.Storage;
import com.mercedesbenz.sechub.storage.core.StorageException;

public abstract class AbstractSharedVolumeStorage implements Storage {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSharedVolumeStorage.class);

    Path volumePath;

    private boolean closed;

    private Path relativePath;

    /**
     * Creates a shared volume storage
     *
     * @param rootLocation
     * @param rootStoragePath
     * @param additionalStoragePathParts
     */
    public AbstractSharedVolumeStorage(Path rootLocation, String rootStoragePath, Object... additionalStoragePathParts) {
        requireNonNull(rootLocation, "rootLocation may not be null");
        requireNonNull(rootStoragePath, "storagePath may not be null");

        this.volumePath = rootLocation.resolve(rootStoragePath);
        if (additionalStoragePathParts != null) {
            for (Object additionalStoragePathPart : additionalStoragePathParts) {
                if (additionalStoragePathPart == null) {
                    LOG.warn("Additional part was null at position: ");
                    continue;
                }
                this.volumePath = volumePath.resolve(additionalStoragePathPart.toString());
            }
        }
        this.relativePath = volumePath.relativize(rootLocation).toAbsolutePath().normalize();

        LOG.debug("Created {} with relative path:{}, volumePath: {}", getClass().getSimpleName(), relativePath, volumePath);
    }

    @Override
    public InputStream fetch(String name) throws IOException {
        LOG.debug("Fetch '{}' from volumePath: {}", name, volumePath);

        assertNotClosed();

        try {
            Path path = getPathToFile(name);
            if (path == null) {
                return null;
            }
            return new FileInputStream(path.toFile());
        } catch (Exception e) {
            throw new IOException("Was not able to fetch: " + name, e);
        }
    }

    @Override
    public void store(String name, InputStream stream) throws IOException {
        store(name, stream, -1);
    }

    @Override
    public void store(String name, InputStream stream, long contentLength) throws IOException {
        requireNonNull(name, "name may not be null!");
        requireNonNull(stream, "stream may not be null!");

        assertNotClosed();

        if (name.contains("..")) {
            // This is a security check
            throw new StorageException("Cannot store file with relative path outside current directory: " + name);
        }

        try {
            Files.createDirectories(volumePath);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage directory at: " + volumePath, e);
        }

        LOG.trace("storing {} in path {}", name, relativePath);

        Path pathToFile = getPathToFile(name);

        try (InputStream inputStream = stream) {
            Files.copy(inputStream, pathToFile, StandardCopyOption.REPLACE_EXISTING);

            LOG.info("Stored: {} at {}", name, pathToFile);
        } catch (Exception e) {
            throw new IOException("Was not able to store input stream into file: " + pathToFile, e);
        }
    }

    @Override
    public void delete(String name) throws IOException {
        assertNotClosed();

        Path path = getPathToFile(name);
        if (!Files.exists(path)) {
            LOG.debug("File '{}' did not exist in volumePath: {}, skip deletion", name, volumePath);
            return;
        }
        Files.delete(path);
        LOG.info("Deleted: {} at {}", name, path);

    }

    public void deleteAll() throws IOException {

        assertNotClosed();

        try {
            if (Files.notExists(volumePath)) {
                return;
            }
            FileSystemUtils.deleteRecursively(volumePath);

            LOG.info("Deleted all inside {}", volumePath);

        } catch (Exception e) {
            throw new IOException("Was not able to delete all from: " + volumePath, e);
        }
    }

    public boolean isExisting(String fileName) {
        assertNotClosed();

        return getPathToFile(fileName).toFile().exists();
    }

    @Override
    public Set<String> listNames() throws IOException {

        assertNotClosed();

        LOG.debug("start listNames for volumePath: {}", volumePath);

        try {
            Set<String> names = new LinkedHashSet<>();
            if (Files.notExists(volumePath)) {
                Files.createDirectories(volumePath);
            }

            Files.list(volumePath).forEach(child -> names.add(child.getFileName().toString()));

            LOG.debug("listNames for volumePath: {} found: {}", volumePath, names);

            return names;
        } catch (Exception e) {
            throw new IOException("Was not able to list names for: " + volumePath, e);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        this.closed = true;
    }

    private Path getPathToFile(String fileName) {
        requireNonNull(fileName, "fileName may not be null!");
        return this.volumePath.resolve(fileName);
    }

    private void assertNotClosed() {
        if (closed) {
            throw new IllegalStateException(getClass().getSimpleName() + " already closed!");
        }

    }

}
