// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class MultiStorageService implements StorageService {

	private static final Logger LOG = LoggerFactory.getLogger(MultiStorageService.class);

	/*
	 * TODO de-jcup, 2019-11-09: think about decoupling this completely:
	 * List<JobStorageFactory> + @Component in storage factories, maybe with init
	 * method (to avoid missing dependency injection in sechub-storage-aws-s3-test
	 * when s3mock starts...). Benefit would be: change between aws and minio s3 impl
	 * would be just a dependency switch and all done. Currently software changes
	 * inside this class are necessary to obtain this.
	 */
	private JobStorageFactory jobStorageFactory;

	@Autowired
	public MultiStorageService(SharedVolumeSetup sharedVolumeSetup, S3Setup s3Setup) {
	    
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
	public JobStorage getJobStorage(String projectId, UUID jobUUID) {
	    /* we use here "jobstorage/${projectId} - so we have same job storage path as before in sechub itself
	     * - for PDS own prefix (storageId) is used insdide storagePath. We could have changed to
	     * something like "sechub/jobstarge/${projectId}" but this would have forced migration issues. So we keep this
	     * "old style"
	     */
		return jobStorageFactory.createJobStorage("jobstorage/"+projectId, jobUUID);
	}

}