// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.core;

import java.util.UUID;

public interface StorageService {

    /**
     * Creates a new job storage object. If you no longer need the job storage
     * object, you have to close the storage object to save resources.
     *
     * @param storagePath defines the storage path. Must be convertible into a valid
     *                    path structure from storage root location. Usage examples:
     *                    Either just a simple identifier (e.g. a project id), or
     *                    something more complex like "pds/gosec-cluster" . When
     *                    <code>null</code> the implementation decides default
     *                    storage path.
     * @param jobUUID     job UUID
     * @return job storage object
     */
    public JobStorage createJobStorageForPath(String storagePath, UUID jobUUID);

    /**
     * Creates a new asset storage object. If you no longer need the asset storage
     * object, you have to close the storage object to save resources.
     *
     * @param assetId asset identifier
     *
     * @return asset storage object
     */
    public AssetStorage createAssetStorage(String assetId);

}