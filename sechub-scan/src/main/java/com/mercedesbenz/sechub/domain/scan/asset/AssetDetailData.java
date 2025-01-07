// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
public class AssetDetailData implements JSONable<AssetDetailData> {

    private String assetId;

    private List<AssetFileData> files = new ArrayList<>();

    public void setAssetId(String assetid) {
        this.assetId = assetid;
    }

    public String getAssetId() {
        return assetId;
    }

    public List<AssetFileData> getFiles() {
        return files;
    }

    @Override
    public Class<AssetDetailData> getJSONTargetClass() {
        return AssetDetailData.class;
    }
}
