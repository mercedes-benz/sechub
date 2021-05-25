// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.core;

import java.util.UUID;

public interface StorageService {

    /**
     * Resolves job storage for given job - same as {@link #getJobStorage(null,
     * UUID)}
     * 
     * @param jobUUID job UUID
     * @return job storage
     */
    public default JobStorage getJobStorage(UUID jobUUID) {
        return getJobStorage(null, jobUUID);
    }

    /**
     * Resolves a job storage
     * 
     * @param storagePath - defines the storage path. Must be a convertible into a
     *                    valid path structure from storage root location. Usage
     *                    examples: Either just a simple identifier (e.g. a project id), or something more
     *                    complex like "pds/gosec-cluster" . When <code>null</code>
     *                    the implementation decides default storage path.
     * @param jobUUID     job UUID
     * @return
     */
    public JobStorage getJobStorage(String storagePath, UUID jobUUID);

}