// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static java.util.Objects.requireNonNull;

class ArchiveSafeguard {

    private static final String DIRECTORY_DELIMITER = "/";

    private final ArchiveInputStream<?> archiveInputStream;
    private final ArchiveSafeguardProperties properties;

    private long entriesCount;
    private long bytesRead;
    private Instant startTime;

    ArchiveSafeguard(ArchiveInputStream<?> archiveInputStream, ArchiveSafeguardProperties archiveSafeguardProperties) {
        this.archiveInputStream = requireNonNull(archiveInputStream, "Property archiveInputStream must not be null");
        properties = requireNonNull(archiveSafeguardProperties, "Property archiveSafeguardProperties must not be null");
    }

    ArchiveSafeguardProperties getProperties() {
        return properties;
    }

    ArchiveInputStream<?> getArchiveInputStream() {
        return archiveInputStream;
    }

    long getEntriesCount() {
        return entriesCount;
    }

    Instant getStartTime() {
        return startTime;
    }

    ArchiveEntry getNextEntry() throws IOException {
        if (startTime == null) {
            startTime = Instant.now();
        }

        if (Duration.between(startTime, Instant.now()).compareTo(properties.getTimeout()) > 0) {
            //throw new IllegalStateException("Timeout exceeded");
        }

        ArchiveEntry entry = archiveInputStream.getNextEntry();
        if (entry == null) {
            return null;
        }

        FileSize maxFileSizeUncompressed = properties.getMaxFileSizeUncompressed();
        bytesRead += archiveInputStream.getBytesRead();
        if (bytesRead > maxFileSizeUncompressed.getBytes()) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed value of %s".formatted(maxFileSizeUncompressed.getSizeString()));
        }

        if (entry.isDirectory()) {
            long maxDirectoryDepth = properties.getMaxDirectoryDepth();
            long directoryDepth = entry.getName().split(DIRECTORY_DELIMITER).length - 1; // Subtract 1 because split includes the file name
            if (entry.isDirectory() && directoryDepth > maxDirectoryDepth) {
                throw new IllegalArgumentException("Directory depth exceeds the maximum allowed value of %s".formatted(maxDirectoryDepth));
            }
        } else {
            long maxEntries = properties.getMaxEntries();
            if (!entry.isDirectory() && ++entriesCount > maxEntries) {
                throw new IllegalArgumentException("Number of entries exceeds the maximum allowed value of %s".formatted(maxEntries));
            }

        }

        return entry;
    }
}
