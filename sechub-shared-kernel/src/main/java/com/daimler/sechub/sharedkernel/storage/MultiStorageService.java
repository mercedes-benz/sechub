// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.daimler.sechub.sharedkernel.storage.filesystem.SharedVolumeJobStorage;
import com.daimler.sechub.sharedkernel.storage.filesystem.SharedVolumeSetup;
import com.daimler.sechub.sharedkernel.storage.s3.AwsS3JobStorage;
import com.daimler.sechub.sharedkernel.storage.s3.S3Setup;

/**
 * MultiStorageService - will provide job storage objects depending on
 * configuration / setup situation. Provides access to a shared volume (e.g. a
 * NFS) or native S3 access via minio
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class MultiStorageService implements StorageService {

	private static final Logger LOG = LoggerFactory.getLogger(MultiStorageService.class);

	private Path sharedVolumeUploadDirectory;
	private S3Setup s3Setup;
	private AmazonS3 s3Client;

	@Autowired
	public MultiStorageService(SharedVolumeSetup sharedVolumeSetup, S3Setup s3Setup) {
		if (!sharedVolumeSetup.isAvailable() && !s3Setup.isAvailable()) {
			throw new IllegalStateException("Neither shared volume setup nor s3 storage setup are valid! At least one must be correct set!");
		}
		this.s3Setup = s3Setup;

		if (s3Setup.isAvailable()) {
			LOG.info("S3 setup available, using S3 buckets for storage");
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Setup.getAccessKey(), s3Setup.getSecretkey());
			ClientConfiguration clientConfiguration = new ClientConfiguration();
			clientConfiguration.setSignerOverride("AWSS3V4SignerType");

			s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds))
					.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Setup.getEndPoint(), Regions.DEFAULT_REGION.name()))
					.withClientConfiguration(clientConfiguration).build();

		} else {
			LOG.info("S3 setup NOT available, using shared volume for storage");
			this.sharedVolumeUploadDirectory = sharedVolumeSetup.getUploadDir() != null ? Paths.get(sharedVolumeSetup.getUploadDir()) : null;

		}

	}

	@Override
	public JobStorage getJobStorage(String projectId, UUID jobUUID) {
		if (s3Setup.isAvailable()) {
			return new AwsS3JobStorage(s3Client, s3Setup.getBucketName(), projectId, jobUUID);
		}
		return new SharedVolumeJobStorage(sharedVolumeUploadDirectory, projectId, jobUUID);
	}

}