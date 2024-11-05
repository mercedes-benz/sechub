// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.storage.core.AssetStorage;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.JobStorageFactory;
import com.mercedesbenz.sechub.storage.core.S3Setup;
import com.mercedesbenz.sechub.storage.core.SharedVolumeSetup;
import com.mercedesbenz.sechub.storage.core.StorageService;
import com.mercedesbenz.sechub.storage.s3.AwsS3JobStorageFactory;
import com.mercedesbenz.sechub.storage.sharevolume.spring.SharedVolumeJobStorageFactory;

@Service
public class PrepareWrapperStorageService implements StorageService {

    private JobStorageFactory jobStorageFactory;

    @Autowired
    public PrepareWrapperStorageService(SharedVolumeSetup sharedVolumeSetup, S3Setup s3Setup) {

        if (s3Setup.isAvailable()) {
            jobStorageFactory = new AwsS3JobStorageFactory(s3Setup);

        } else if (sharedVolumeSetup.isAvailable()) {
            jobStorageFactory = new SharedVolumeJobStorageFactory(sharedVolumeSetup);
        }

        if (jobStorageFactory == null) {
            throw new IllegalStateException("Did not found any available storage setup! At least one must be set!");
        }
    }

    @Override
    public JobStorage createJobStorage(String storagePath, UUID jobUUID) {
        return jobStorageFactory.createJobStorage(storagePath, jobUUID);
    }

    @Override
    public AssetStorage createAssetStorage(String assetId) {
        throw new IllegalStateException("The prepare wrapper does not support access to assets at the moment (there is no need)!");
    }

}
