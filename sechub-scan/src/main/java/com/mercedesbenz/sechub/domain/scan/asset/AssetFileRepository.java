// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import static com.mercedesbenz.sechub.domain.scan.asset.AssetFile.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mercedesbenz.sechub.domain.scan.asset.AssetFile.AssetFileCompositeKey;

public interface AssetFileRepository extends JpaRepository<AssetFile, AssetFileCompositeKey> {

    @Query(value = "SELECT DISTINCT " + COLUMN_ASSET_ID + " FROM " + TABLE_NAME, nativeQuery = true)
    List<String> fetchAllAssetIds();

    @Query(value = "SELECT * from " + TABLE_NAME + " WHERE " + COLUMN_ASSET_ID + "=:assetId", nativeQuery = true)
    List<AssetFile> fetchAllAssetFilesWithAssetId(@Param("assetId") String assetId);

    @Modifying
    @Query(value = "DELETE from " + TABLE_NAME + " WHERE " + COLUMN_ASSET_ID + "=:assetId", nativeQuery = true)
    void deleteAssetFilesHavingAssetId(@Param("assetId") String assetId);

}
