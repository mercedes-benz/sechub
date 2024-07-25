// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

public class ArchiveExtractionResult {
    int extractedFilesCount;
    int createdFoldersCount;
    long size;

    String sourceLocation;
    String targetLocation;

    public int getExtractedFilesCount() {
        return extractedFilesCount;
    }

    public int getCreatedFoldersCount() {
        return createdFoldersCount;
    }

    public long getSize() {
        return size;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getTargetLocation() {
        return targetLocation;
    }
}