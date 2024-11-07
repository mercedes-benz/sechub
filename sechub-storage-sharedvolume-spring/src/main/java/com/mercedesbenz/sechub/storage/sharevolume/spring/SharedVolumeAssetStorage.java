// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.sharevolume.spring;

import static java.util.Objects.*;

import java.nio.file.Path;

import com.mercedesbenz.sechub.storage.core.AssetStorage;

public class SharedVolumeAssetStorage extends AbstractSharedVolumeStorage implements AssetStorage {

    public SharedVolumeAssetStorage(Path rootLocation, String assetId) {
        super(rootLocation, "assets", requireNonNull(assetId, "assetId may not be null"));
    }

}
