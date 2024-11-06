// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercedesbenz.sechub.domain.scan.asset.AssetFile.AssetFileCompositeKey;

public interface AssetFileRepository extends JpaRepository<AssetFile, AssetFileCompositeKey> {


}
