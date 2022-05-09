package com.mercedesbenz.sechub.commons.archive;

public class ArchivePathInspectionResult {
    boolean accepted;
    String wantedPath;

    public ArchivePathInspectionResult() {
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getWantedPath() {
        return wantedPath;
    }

    public boolean isPathChangeWanted() {
        if (wantedPath == null) {
            return false;
        }
        return true;
    }
}