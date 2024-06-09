// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.time.Duration;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * The ArchiveSafeguardProperties class encapsulates the properties used to safeguard the extraction of an archive.
 * These properties include maximum file size, maximum uncompressed file size, maximum number of entries, maximum compression rate, maximum directory depth, and timeout.
 *
 * <p> Each property is validated during the creation of an ArchiveSafeguardProperties object to ensure they meet the required conditions.
 *
 * <p> Example usage:
 * <pre>
 *     FileSize maxFileSize = new FileSize("10MB");
 *     FileSize maxFileSizeUncompressed = new FileSize("20MB");
 *     long maxEntries = 100;
 *     FileSize maxCompressionRate = new FileSize("50MB");
 *     long maxDirectoryDepth = 5;
 *     Duration timeout = Duration.ofMinutes(1);
 *
 *     ArchiveSafeguardProperties properties = new ArchiveSafeguardProperties(maxFileSize, maxFileSizeUncompressed, maxEntries, maxCompressionRate, maxDirectoryDepth, timeout);
 * </pre>
 *
 * @author hamidonos
 */
public class ArchiveSafeguardProperties {
    private final FileSize maxFileSize;
    private final FileSize maxFileSizeUncompressed;
    private final long maxEntries;
    private final FileSize maxCompressionRate;
    private final long maxDirectoryDepth;
    private final Duration timeout;

    public ArchiveSafeguardProperties(FileSize maxFileSize,
                                      FileSize maxFileSizeUncompressed,
                                      long maxEntries,
                                      FileSize maxCompressionRate,
                                      long maxDirectoryDepth,
                                      Duration timeout) {
        this.maxFileSize = requireNonNull(maxFileSize, "Property maxFileSize must not be null");
        this.maxFileSizeUncompressed = requireNonNull(maxFileSizeUncompressed, "Property maxFileSizeUncompressed must not be null");
        this.maxEntries = maxEntries;
        if (this.maxEntries <= 0) {
            throw new IllegalArgumentException("Property maxEntries must be greater than 0");
        }
        this.maxCompressionRate = requireNonNull(maxCompressionRate, "Property maxCompressionRate must not be null");
        this.maxDirectoryDepth = maxDirectoryDepth;
        if (this.maxDirectoryDepth <= 0) {
            throw new IllegalArgumentException("Property maxDirectoryDepth must be greater than 0");
        }
        this.timeout = requireNonNull(timeout, "Property timeout must not be null");
        if (this.timeout.isNegative() || this.timeout.isZero()) {
            throw new IllegalArgumentException("Property timeout must be greater than 0");
        }
    }

    public FileSize getMaxFileSize() {
        return maxFileSize;
    }

    public FileSize getMaxFileSizeUncompressed() {
        return maxFileSizeUncompressed;
    }

    public long getMaxEntries() {
        return maxEntries;
    }

    public FileSize getMaxCompressionRate() {
        return maxCompressionRate;
    }

    public long getMaxDirectoryDepth() {
        return maxDirectoryDepth;
    }

    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArchiveSafeguardProperties that)) {
            return false;
        }
        return Objects.equals(maxFileSize, that.maxFileSize) &&
                Objects.equals(maxFileSizeUncompressed, that.maxFileSizeUncompressed) &&
                maxEntries == that.maxEntries &&
                Objects.equals(maxCompressionRate, that.maxCompressionRate) &&
                maxDirectoryDepth == that.maxDirectoryDepth &&
                Objects.equals(timeout, that.timeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxFileSize, maxFileSizeUncompressed, maxEntries, maxCompressionRate, maxDirectoryDepth, timeout);
    }
}
