package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.storage.core.*;
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
    public JobStorage getJobStorage(String storagePath, UUID jobUUID) {
        JobStorage jobStorage = jobStorageFactory.createJobStorage(storagePath, jobUUID);

        return jobStorage;
    }

}
