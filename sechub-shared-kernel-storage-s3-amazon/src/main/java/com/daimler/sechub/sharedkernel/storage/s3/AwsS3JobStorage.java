package com.daimler.sechub.sharedkernel.storage.s3;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.daimler.sechub.sharedkernel.storage.core.JobStorage;

public class AwsS3JobStorage implements JobStorage {

	private static final Logger LOG = LoggerFactory.getLogger(AwsS3JobStorage.class);

	private AmazonS3 client;
	private String bucketName;
	private String projectId;
	private UUID jobUUID;

	public AwsS3JobStorage(AmazonS3 client, String bucketName, String projectId, UUID jobUUID) {
		this.bucketName = bucketName;
		this.client = client;
		this.projectId = projectId;
		this.jobUUID = jobUUID;
	}

	@Override
	public void store(String name, InputStream inputStream) throws IOException {
		requireNonNull(name, "name may not be null!");
		requireNonNull(inputStream, "inputStream may not be null!");

		try (InputStream stream=inputStream){
			if (!client.doesBucketExistV2(bucketName)) {
				client.createBucket(bucketName);
			}
			ObjectMetadata meta = new ObjectMetadata();
			client.putObject(bucketName, getObjectName(name), stream, meta);

		} catch (Exception e) {
			throw new IOException("Store of " + name + " to s3 failed", e);
		}

	}


	private String getObjectName(String name) {
		return getObjectPrefix() + name;
	}

	private String getObjectPrefix() {
		return "jobstorage/" + projectId + "/" + jobUUID + "/";
	}

	@Override
	public InputStream fetch(String name) throws IOException {
		try {
			return client.getObject(bucketName, getObjectName(name)).getObjectContent();
		} catch (Exception e) {
			throw new IOException("Was not able to fetch object from s3 bucket:" + name);
		}
	}

	@Override
	public void deleteAll() throws IOException {
		String objectPrefix = getObjectPrefix();
		LOG.info("delete all job storage parts from prefix:{}",objectPrefix);


		ObjectListing listing = client.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(objectPrefix));
	    try {
	    	while (listing != null) {
	    		List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>(listing.getObjectSummaries().size());
	    		for (S3ObjectSummary summary : listing.getObjectSummaries()) {
	    			String key = summary.getKey();
	    			LOG.debug("add key to deletion batch, key={}", key);
	    			keys.add(new DeleteObjectsRequest.KeyVersion(key));
	    		}
	    		if (!keys.isEmpty()) {
	    			LOG.debug("delete key batch");
	    			client.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(keys));
	    		}
	    		if (!listing.isTruncated()) return;

	    		listing = client.listNextBatchOfObjects(listing);
	    	}
		}catch(RuntimeException e) {
			throw new IOException("Cannot delete all parts from prefix:"+objectPrefix,e);
		}
	}

	@Override
	public boolean isExisting(String name) throws IOException {
		try {
			return client.doesObjectExist(bucketName, getObjectName(name));
		}catch(RuntimeException e) {
			throw new IOException("Cannot check existence",e);
		}
	}

}
