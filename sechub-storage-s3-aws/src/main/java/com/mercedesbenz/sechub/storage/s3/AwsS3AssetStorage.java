// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.mercedesbenz.sechub.storage.core.AssetStorage;

public class AwsS3AssetStorage extends AbstractAwsS3Storage implements AssetStorage {

    public AwsS3AssetStorage(AmazonS3 client, String bucketName, String assetId) {
        super(client, bucketName, "assets", assetId);
    }
    
}
