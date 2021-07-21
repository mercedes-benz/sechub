// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.storage;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.storage.core.JobStorage;
import com.daimler.sechub.storage.core.JobStorageFactory;
import com.daimler.sechub.storage.core.S3Setup;
import com.daimler.sechub.storage.core.SharedVolumeSetup;
import com.daimler.sechub.storage.core.StorageService;
import com.daimler.sechub.storage.s3.aws.AwsS3JobStorageFactory;
import com.daimler.sechub.storage.sharevolume.spring.SharedVolumeJobStorageFactory;

/**
 * MultiStorageService - will provide job storage objects depending on
 * configuration / setup situation. Provides access to a shared volume (e.g. a
 * NFS) or native S3 access
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class PDSMultiStorageService implements StorageService {

    @Autowired
    PDSServerConfigurationService serverConfigurationService;

    private static final Logger LOG = LoggerFactory.getLogger(PDSMultiStorageService.class);

    private JobStorageFactory jobStorageFactory;

    @Autowired
    public PDSMultiStorageService(SharedVolumeSetup sharedVolumeSetup, S3Setup s3Setup) {

        if (s3Setup.isAvailable()) {
            jobStorageFactory = new AwsS3JobStorageFactory(s3Setup);

        } else if (sharedVolumeSetup.isAvailable()) {
            jobStorageFactory = new SharedVolumeJobStorageFactory(sharedVolumeSetup);

        }

        if (jobStorageFactory == null) {
            throw new IllegalStateException("Did not found any available storage setup! At least one must be set!");
        }
        LOG.info("Created storage factory: {}", jobStorageFactory.getClass().getSimpleName());

    }

    @Override
    public JobStorage getJobStorage(String storagePath, UUID jobUUID) {
        if (storagePath == null) {
            storagePath = serverConfigurationService.getStorageId();
            LOG.debug("storage path parameter was null - fallback to default:{}", storagePath);
        }
        JobStorage jobStorage = jobStorageFactory.createJobStorage(storagePath, jobUUID);
        
        return jobStorage;
    }

}