// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.storage.filesystem.SharedVolumeJobStorage;
import com.daimler.sechub.sharedkernel.storage.filesystem.SharedVolumeSetup;
import com.daimler.sechub.sharedkernel.storage.s3.MinioS3JobStorage;
import com.daimler.sechub.sharedkernel.storage.s3.S3Setup;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

/**
 * MultiStorageService - will provide job storage objects depending on
 * configuration situation. Provides access to a shared volume (e.g. a NFS) or native S3 access via minio
 * @author Albert Tregnaghi
 *
 */
@Service
public class MultiStorageService implements StorageService {

    private final Path sharedVolumeUploadDirectory;
	private S3Setup s3Setup;
	private MinioClient minioClient;

    @Autowired
    public MultiStorageService(SharedVolumeSetup sharedVolumeSetup, S3Setup s3Setup) {
        if (! sharedVolumeSetup.isAvailable() && ! s3Setup.isAvailable()) {
        	throw new IllegalStateException("Neither shared volume setup nor s3 storage setup are valid! At least one must be correct set!");
        }
    	this.sharedVolumeUploadDirectory = Paths.get(sharedVolumeSetup.getUploadDir());
    	this.s3Setup = s3Setup;

    	if (s3Setup.isAvailable()) {
    		try {
				minioClient=new MinioClient(s3Setup.getEndPoint(),s3Setup.getAccessKey(), s3Setup.getSecretkey());
			} catch (InvalidEndpointException | InvalidPortException e) {
				throw new IllegalStateException("Minio client setup failed!",e);
			}

    	}

    }

    @Override
    public JobStorage getJobStorage(String projectId, UUID jobUUID) {
    	if (s3Setup.isAvailable()) {
    		return new MinioS3JobStorage(minioClient,s3Setup.getBucketName(), projectId,jobUUID);
    	}
    	return new SharedVolumeJobStorage(sharedVolumeUploadDirectory, projectId,jobUUID);
    }


}