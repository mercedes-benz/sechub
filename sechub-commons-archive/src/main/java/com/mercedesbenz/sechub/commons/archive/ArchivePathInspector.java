package com.mercedesbenz.sechub.commons.archive;

public interface ArchivePathInspector {

    /**
     * Inspect and returns result or <code>null</code>
     *
     * @param path
     * @return inspection result or <code>null</code> when no filter is necessary
     */
    ArchivePathInspectionResult inspect(String path);

}
