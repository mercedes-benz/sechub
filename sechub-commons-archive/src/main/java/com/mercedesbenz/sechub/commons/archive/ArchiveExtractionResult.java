package com.mercedesbenz.sechub.commons.archive;

public class ArchiveExtractionResult {
    int extractedFilesCount;
    int createdFoldersCount;

    String sourceLocation;
    String targetLocation;

    public int getExtractedFilesCount() {
        return extractedFilesCount;
    }

    public int getCreatedFoldersCount() {
        return createdFoldersCount;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getTargetLocation() {
        return targetLocation;
    }
}