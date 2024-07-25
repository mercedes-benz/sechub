// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

class SafeArchiveInputStream extends InputStream {
    private static final String DIRECTORY_DELIMITER = "/";

    private final ArchiveInputStream<?> archiveInputStream;
    private final ArchiveExtractionConstraints archiveExtractionConstraints;

    private Instant startTime;
    private long entriesCount;
    private long bytesRead;

    public SafeArchiveInputStream(ArchiveInputStream<?> archiveInputStream, ArchiveExtractionConstraints archiveExtractionConstraints) {
        this.archiveInputStream = requireNonNull(archiveInputStream, "Property archiveInputStream must not be null");
        this.archiveExtractionConstraints = requireNonNull(archiveExtractionConstraints, "Property archiveExtractionConstraints must not be null");
    }

    @Override
    public int read() throws IOException {
        int result = archiveInputStream.read();
        if (result != -1) {
            bytesRead++;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = archiveInputStream.read(b, off, len);
        if (result != -1) {
            bytesRead += result;
        }
        return result;
    }

    ArchiveExtractionConstraints getArchiveExtractionConstraints() {
        return archiveExtractionConstraints;
    }

    long getEntriesCount() {
        return entriesCount;
    }

    Instant getStartTime() {
        return startTime;
    }

    long getBytesRead() {
        return bytesRead;
    }

    ArchiveEntry getNextEntry() throws IOException {
        if (startTime == null) {
            startTime = Instant.now();
        }

        if (Duration.between(startTime, Instant.now()).compareTo(archiveExtractionConstraints.getTimeout()) > 0) {
            throw new ArchiveExtractionException("Timeout exceeded");
        }

        FileSize maxFileSizeUncompressed = archiveExtractionConstraints.getMaxFileSizeUncompressed();
        if (bytesRead > maxFileSizeUncompressed.getBytes()) {
            throw new ArchiveExtractionException("File size exceeds the maximum allowed value of %s".formatted(maxFileSizeUncompressed.getSizeString()));
        }

        ArchiveEntry entry = archiveInputStream.getNextEntry();
        if (entry == null) {
            return null;
        }

        long maxDirectoryDepth = archiveExtractionConstraints.getMaxDirectoryDepth();
        long directoryDepth = entry.getName().split(DIRECTORY_DELIMITER).length - 1; // Subtract 1 because split includes the file name
        if (directoryDepth > maxDirectoryDepth) {
            throw new ArchiveExtractionException("Directory depth exceeds the maximum allowed value of %s".formatted(maxDirectoryDepth));
        }

        if (!entry.isDirectory()) {
            long maxEntries = archiveExtractionConstraints.getMaxEntries();
            if (++entriesCount > maxEntries) {
                throw new ArchiveExtractionException("Number of entries exceeds the maximum allowed value of %s".formatted(maxEntries));
            }
        }

        return entry;
    }
}