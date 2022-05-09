package com.mercedesbenz.sechub.commons.archive;

public interface ArchivePathInspector {

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

    /**
     * Inspect and returns result or <code>null</code>
     *
     * @param path
     * @return inspection result or <code>null</code> when no filter is necessary
     */
    ArchivePathInspectionResult inspect(String path);

}
