// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.core;

import java.util.UUID;

public interface JobStorageFactory {

    /**
     * Creates a new job storage for given storagePath and job
     *
     * @param storagePath - defines the storage path. Must be a convertible into a
     *                    valid path structure from storage root location. Usage
     *                    examples:, e.g. just simple project id, or something more
     *                    complex like "pds/gosec-cluster"
     * @param jobUUID
     * @return job storage, never <code>null</code>
     */
    public JobStorage createJobStorage(String storagePath, UUID jobUUID);

}
