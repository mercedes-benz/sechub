// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.storage;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.util.SecHubStorageUtil;
import com.mercedesbenz.sechub.storage.core.AssetStorage;
import com.mercedesbenz.sechub.storage.core.AssetStorageFactory;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.JobStorageFactory;
import com.mercedesbenz.sechub.storage.core.S3Setup;
import com.mercedesbenz.sechub.storage.core.SharedVolumeSetup;
import com.mercedesbenz.sechub.storage.s3.AwsS3JobStorageFactory;
import com.mercedesbenz.sechub.storage.sharevolume.spring.SharedVolumeJobStorageFactory;

/**
 * MultiStorageService - will provide job storage objects depending on
 * configuration / setup situation. Provides access to a shared volume (e.g. a
 * NFS) or native S3 access
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class MultiStorageService implements SecHubStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(MultiStorageService.class);

    private JobStorageFactory jobStorageFactory;
    private AssetStorageFactory assetStorageFactory;

    @Autowired
    public MultiStorageService(SharedVolumeSetup sharedVolumeSetup, S3Setup s3Setup) {

        if (s3Setup.isAvailable()) {
            AwsS3JobStorageFactory awsJobFactory = new AwsS3JobStorageFactory(s3Setup);

            jobStorageFactory = awsJobFactory;
            assetStorageFactory = awsJobFactory;

        } else if (sharedVolumeSetup.isAvailable()) {
            SharedVolumeJobStorageFactory sharedVolumeStorageFactory = new SharedVolumeJobStorageFactory(sharedVolumeSetup);
            jobStorageFactory = sharedVolumeStorageFactory;
            assetStorageFactory = sharedVolumeStorageFactory;

        }

        if (jobStorageFactory == null || assetStorageFactory == null) {
            throw new IllegalStateException("Did not found any available storage setup! At least one must be set!");
        }
        LOG.info("Created job storage factory: {}", jobStorageFactory.getClass().getSimpleName());
        LOG.info("Created asset storage factory: {}", assetStorageFactory.getClass().getSimpleName());

    }

    @Override
    public JobStorage createJobStorageForPath(String storagePath, UUID jobUUID) {
        return jobStorageFactory.createJobStorage(storagePath, jobUUID);
    }

    @Override
    public AssetStorage createAssetStorage(String storagePath, String assetId) {
        return assetStorageFactory.createAssetStorage(storagePath, assetId);
    }

}