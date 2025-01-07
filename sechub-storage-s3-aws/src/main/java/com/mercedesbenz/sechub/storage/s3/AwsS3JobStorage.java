// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import java.util.UUID;

import com.amazonaws.services.s3.AmazonS3;
import com.mercedesbenz.sechub.storage.core.JobStorage;

public class AwsS3JobStorage extends AbstractAwsS3Storage implements JobStorage {

    public AwsS3JobStorage(AmazonS3 client, String bucketName, String storagePath, UUID jobUUID) {
        super(client, bucketName, storagePath, jobUUID.toString());
    }

    public AwsS3JobStorage(AmazonS3 client, String bucketName, String storagePath, UUID jobUUID, TransferManagerFactory transferManagerFactory) {
        super(client, bucketName, storagePath, jobUUID.toString(), transferManagerFactory);
    }
}
