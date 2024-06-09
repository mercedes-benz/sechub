// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.util.Objects;

import static java.lang.Long.parseLong;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

/**
 * The FileSize class represents a file size in a human-readable format (KB, MB, GB) and its equivalent size in bytes.
 * It provides methods to get the file size as a string and in bytes.
 *
 * <p> The class supports file sizes in kilobytes (KB), megabytes (MB), and gigabytes (GB).
 * The size string is case-insensitive and should end with either KB, MB, or GB.
 *
 * <p> Example usage:
 * <pre>
 *     FileSize fileSize = new FileSize("10MB");
 *     System.out.println(fileSize.getSizeString()); // Prints: 10MB
 *     System.out.println(fileSize.getSizeBytes()); // Prints: 10485760
 * </pre>
 * <pre>
 *     FileSize fileSize = new FileSize("10gb");
 *     System.out.println(fileSize.getSizeString()); // Prints: 10GB
 *     System.out.println(fileSize.getSizeBytes()); // Prints: 10737418240
 * </pre>
 *
 * <p> Note: This class does not support file sizes in terabytes (TB) or higher, or sizes without a unit.
 *
 * @author hamidonos
 */
public class FileSize {
    private static final String FILE_SIZE_KILOBYTES = "KB";
    private static final String FILE_SIZE_MEGABYTES = "MB";
    private static final String FILE_SIZE_GIGABYTES = "GB";

    private final String sizeStr;
    private final long sizeBytes;

    public FileSize(String sizeStr) {
        this.sizeStr = convertSizeString(requireNonNull(sizeStr, "sizeStr property must not be null"));
        sizeBytes = convertToBytes(this.sizeStr);
    }

    public String getSizeString() {
        return sizeStr;
    }

    public long getBytes() {
        return sizeBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileSize fileSize)) {
            return false;
        }
        return Objects.equals(sizeStr, fileSize.sizeStr) && sizeBytes == fileSize.sizeBytes;
    }

    @Override
    public int hashCode() {
        return hash(sizeStr, sizeBytes);
    }

    private static String convertSizeString(String sizeStr) {
        return sizeStr.toUpperCase();
    }

    private static long convertToBytes(String sizeStr) {
        long size;
        long multiplier;

        if (sizeStr.endsWith(FILE_SIZE_KILOBYTES)) {
            multiplier = 1024;
        } else if (sizeStr.endsWith(FILE_SIZE_MEGABYTES)) {
            multiplier = 1024 * 1024;
        } else if (sizeStr.endsWith(FILE_SIZE_GIGABYTES)) {
            multiplier = 1024 * 1024 * 1024;
        } else {
            throw new IllegalArgumentException("Invalid file size %s. Must end with KB, MB, or GB.".formatted(sizeStr));
        }

        size = parseLong(sizeStr.substring(0, sizeStr.length() - 2));

        if (size <= 0) {
            throw new IllegalArgumentException("File size must be greater than 0");
        }

        return size * multiplier;
    }
}