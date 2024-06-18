// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.time.Duration;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * The SafeArchiveInputStreamProperties class encapsulates the properties used to safeguard the extraction of an archive
 * when using the {@link SafeArchiveInputStream}.
 * These properties include maximum uncompressed file size, maximum number of entries, maximum directory depth, and timeout.
 *
 * <p> Each property is validated during the creation of an SafeArchiveInputStreamProperties object to ensure they meet the required conditions.
 *
 * <p> Example usage:
 * <pre>
 *     FileSize maxFileSizeUncompressed = new FileSize("20MB");
 *     long maxEntries = 100;
 *     long maxDirectoryDepth = 5;
 *     Duration timeout = Duration.ofMinutes(1);
 *
 *     SafeArchiveInputStreamProperties properties = new SafeArchiveInputStreamProperties(maxFileSize, maxFileSizeUncompressed, maxEntries, maxCompressionRate, maxDirectoryDepth, timeout);
 * </pre>
 *
 * @author hamidonos
 */
public class ArchiveExtractionContext {
    private final FileSize maxFileSizeUncompressed;
    private final long maxEntries;
    private final long maxDirectoryDepth;
    private final Duration timeout;

    public ArchiveExtractionContext(FileSize maxFileSizeUncompressed,
                                    long maxEntries,
                                    long maxDirectoryDepth,
                                    Duration timeout) {
        this.maxFileSizeUncompressed = requireNonNull(maxFileSizeUncompressed, "Property maxFileSizeUncompressed must not be null");
        this.maxEntries = maxEntries;
        if (this.maxEntries <= 0) {
            throw new IllegalArgumentException("Property maxEntries must be greater than 0");
        }
        this.maxDirectoryDepth = maxDirectoryDepth;
        if (this.maxDirectoryDepth <= 0) {
            throw new IllegalArgumentException("Property maxDirectoryDepth must be greater than 0");
        }
        this.timeout = requireNonNull(timeout, "Property timeout must not be null");
        if (this.timeout.isNegative() || this.timeout.isZero()) {
            throw new IllegalArgumentException("Property timeout must be greater than 0");
        }
    }

    public FileSize getMaxFileSizeUncompressed() {
        return maxFileSizeUncompressed;
    }

    public long getMaxEntries() {
        return maxEntries;
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
        if (!(o instanceof ArchiveExtractionContext that)) {
            return false;
        }
        return Objects.equals(maxFileSizeUncompressed, that.maxFileSizeUncompressed) &&
                Objects.equals(maxEntries, that.maxEntries) &&
                Objects.equals(maxDirectoryDepth, that.maxDirectoryDepth) &&
                Objects.equals(timeout, that.timeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth, timeout);
    }
}
