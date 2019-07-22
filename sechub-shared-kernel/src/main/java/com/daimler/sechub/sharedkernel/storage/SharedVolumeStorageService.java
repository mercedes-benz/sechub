// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SharedVolumeStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public SharedVolumeStorageService(SharedVolumeSetup properties) {
        this.rootLocation = Paths.get(properties.getUploadDir());
    }
    
    @Override
    public JobStorage getJobStorage(String projectId, UUID jobUUID) {
    	return new JobStorage(rootLocation, projectId,jobUUID);
    }

   
}