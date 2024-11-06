// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.sharevolume.spring;

import static java.util.Objects.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.mercedesbenz.sechub.storage.core.AssetStorage;
import com.mercedesbenz.sechub.storage.core.AssetStorageFactory;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.JobStorageFactory;
import com.mercedesbenz.sechub.storage.core.SharedVolumeSetup;

public class SharedVolumeJobStorageFactory implements JobStorageFactory, AssetStorageFactory {
    private Path sharedVolumeUploadDirectory;

    public SharedVolumeJobStorageFactory(SharedVolumeSetup sharedVolumeSetup) {
        requireNonNull(sharedVolumeSetup, "sharedVolumeSetup may not be null!");
        if (!sharedVolumeSetup.isAvailable()) {
            throw new IllegalStateException("Shared Volume setup not available!");
        }
        this.sharedVolumeUploadDirectory = sharedVolumeSetup.getUploadDir() != null ? Paths.get(sharedVolumeSetup.getUploadDir()) : null;
    }

    @Override
    public JobStorage createJobStorage(String projectId, UUID jobUUID) {
        return new SharedVolumeJobStorage(sharedVolumeUploadDirectory, projectId, jobUUID);
    }

    @Override
    public AssetStorage createAssetStorage(String storagePath, String assetId) {
        return new SharedVolumeAssetStorage(sharedVolumeUploadDirectory, storagePath, assetId);
    }

}
