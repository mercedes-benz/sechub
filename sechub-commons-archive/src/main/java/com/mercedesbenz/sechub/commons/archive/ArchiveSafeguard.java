// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static java.util.Objects.requireNonNull;

class ArchiveSafeguard {

    private final ArchiveInputStream<?> inputStream;
    private final ArchiveSafeguardProperties properties;

    private long entriesCount;
    private long directoryDepthCount;
    private Instant startTime;

    ArchiveSafeguard(ArchiveInputStream<?> inputStream, ArchiveSafeguardProperties properties) {
        this.inputStream = requireNonNull(inputStream, "Property inputStream must not be null");
        this.properties = requireNonNull(properties, "Property properties must not be null");
    }

    ArchiveSafeguardProperties getProperties() {
        return properties;
    }

    ArchiveInputStream<?> getInputStream() {
        return inputStream;
    }

    long getEntriesCount() {
        return entriesCount;
    }

    long getDirectoryDepthCount() {
        return directoryDepthCount;
    }

    Instant getStartTime() {
        return startTime;
    }

    ArchiveEntry getNextEntry() throws IOException {
        if (startTime == null) {
            startTime = Instant.now();
        }

        // Check if the timeout has been exceeded

        if (Duration.between(startTime, Instant.now()).compareTo(properties.getTimeout()) > 0) {
            throw new IllegalStateException("Timeout exceeded");
        }

        ArchiveEntry entry = inputStream.getNextEntry();
        if (entry == null) {
            return null;
        }

        // Check if the file size exceeds the maximum allowed size

        FileSize maxFileSizeUncompressed = properties.getMaxFileSizeUncompressed();
        if (inputStream.getBytesRead() > maxFileSizeUncompressed.getBytes()) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed value of %s".formatted(maxFileSizeUncompressed.getSizeString()));
        }

        // Check if the number of entries exceeds the maximum allowed number of entries

        long maxEntries = properties.getMaxEntries();
        if (++entriesCount > maxEntries) {
            throw new IllegalArgumentException("Number of entries exceeds the maximum allowed value of %s".formatted(maxEntries));
        }

        // Check if the directory depth exceeds the maximum allowed directory depth

        long maxDirectoryDepth = properties.getMaxDirectoryDepth();
        if (entry.isDirectory() && ++directoryDepthCount > maxDirectoryDepth) {
            throw new IllegalArgumentException("Directory depth exceeds the maximum allowed value of %s".formatted(maxDirectoryDepth));
        }

        return entry;
    }
}
