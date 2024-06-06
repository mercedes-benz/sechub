// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

public class ArchiveExtractionSafeGuard {
    private final FileSize maxFileSize;
    private final FileSize maxFileSizeUncompressed;
    private final long maxEntries;
    private final FileSize maxCompressionRate;
    private final Duration timeout;

    public ArchiveExtractionSafeGuard(final FileSize maxFileSize,
                                      final FileSize maxFileSizeUncompressed,
                                      final long maxEntries,
                                      final FileSize maxCompressionRate,
                                      final Duration timeout) {
        this.maxFileSize = requireNonNull(maxFileSize, "Property maxFileSize must not be null");
        this.maxFileSizeUncompressed = requireNonNull(maxFileSizeUncompressed, "Property maxFileSizeUncompressed must not be null");
        this.maxEntries = maxEntries;
        this.maxCompressionRate = requireNonNull(maxCompressionRate, "Property maxCompressionRate must not be null");
        this.timeout = requireNonNull(timeout, "Property timeout must not be null");

        if (maxFileSizeUncompressed.getSizeBytes() <= 0) {
            throw new IllegalArgumentException("Property maxFileSizeUncompressed must be greater than 0");
        }

        if (maxEntries <= 0) {
            throw new IllegalArgumentException("Property maxEntries must be greater than 0");
        }

        if (maxCompressionRate.getSizeBytes() <= 0) {
            throw new IllegalArgumentException("Property maxCompressionRate must be greater than 0");
        }
    }

    public FileSize getMaxFileSize() {
        return maxFileSize;
    }

    public FileSize getMaxFileSizeUncompressed() {
        return maxFileSizeUncompressed;
    }

    public Long getMaxEntries() {
        return maxEntries;
    }

    public FileSize getMaxCompressionRate() {
        return maxCompressionRate;
    }

    public Duration getTimeout() {
        return timeout;
    }
}
