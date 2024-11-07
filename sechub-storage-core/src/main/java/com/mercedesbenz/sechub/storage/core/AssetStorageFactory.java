// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.core;

public interface AssetStorageFactory {

    /**
     * Creates a new asset storage for given asset id
     *
     * @param assetId asset identifier
     *
     * @return asset storage, never <code>null</code>
     */
    public AssetStorage createAssetStorage(String assetId);

}
