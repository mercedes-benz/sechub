package com.mercedesbenz.sechub.commons.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

import static java.util.Objects.requireNonNull;

class SafeArchiveInputStream extends InputStream {
    private static final String DIRECTORY_DELIMITER = "/";

    private final ArchiveInputStream<?> inputStream;
    private final ArchiveExtractionContext properties;

    private Instant startTime;
    private long entriesCount;
    private long bytesRead = 0;

    public SafeArchiveInputStream(ArchiveInputStream<?> inputStream, ArchiveExtractionContext properties) {
        this.inputStream = requireNonNull(inputStream, "Property inputStream must not be null");
        this.properties = requireNonNull(properties, "Property properties must not be null");
    }

    @Override
    public int read() throws IOException {
        int result = inputStream.read();
        if (result != -1) {
            bytesRead++;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = inputStream.read(b, off, len);
        if (result != -1) {
            bytesRead += result;
        }
        return result;
    }

    ArchiveExtractionContext getProperties() {
        return properties;
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

        if (Duration.between(startTime, Instant.now()).compareTo(properties.getTimeout()) > 0) {
            throw new IllegalStateException("Timeout exceeded");
        }

        FileSize maxFileSizeUncompressed = properties.getMaxFileSizeUncompressed();
        if (bytesRead > maxFileSizeUncompressed.getBytes()) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed value of %s".formatted(maxFileSizeUncompressed.getSizeString()));
        }

        ArchiveEntry entry = inputStream.getNextEntry();
        if (entry == null) {
            return null;
        }

        long maxDirectoryDepth = properties.getMaxDirectoryDepth();
        long directoryDepth = entry.getName().split(DIRECTORY_DELIMITER).length - 1; // Subtract 1 because split includes the file name
        if (directoryDepth > maxDirectoryDepth) {
            throw new IllegalArgumentException("Directory depth exceeds the maximum allowed value of %s".formatted(maxDirectoryDepth));
        }

        if (!entry.isDirectory()) {
            long maxEntries = properties.getMaxEntries();
            if (!entry.isDirectory() && ++entriesCount > maxEntries) {
                throw new IllegalArgumentException("Number of entries exceeds the maximum allowed value of %s".formatted(maxEntries));
            }
        }

        return entry;
    }
}