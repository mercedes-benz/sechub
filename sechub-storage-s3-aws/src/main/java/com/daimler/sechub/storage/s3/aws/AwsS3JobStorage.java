// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.s3.aws;

import static java.util.Objects.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.daimler.sechub.storage.core.JobStorage;

public class AwsS3JobStorage implements JobStorage {

    private static final Logger LOG = LoggerFactory.getLogger(AwsS3JobStorage.class);

    private AmazonS3 client;
    private String bucketName;
    private String storagePath;
    private UUID jobUUID;

    /**
     * Creates a new AWS S3 storage object
     * 
     * @param client
     * @param bucketName
     * @param storagePath
     * @param jobUUID
     */
    public AwsS3JobStorage(AmazonS3 client, String bucketName, String storagePath, UUID jobUUID) {
        this.bucketName = bucketName;
        this.client = client;
        this.storagePath = storagePath;
        this.jobUUID = jobUUID;
    }

    @Override
    public void store(String name, InputStream inputStream) throws IOException {
        requireNonNull(name, "name may not be null!");
        requireNonNull(inputStream, "inputStream may not be null!");

        try (InputStream stream = inputStream) {
            if (!client.doesBucketExistV2(bucketName)) {
                client.createBucket(bucketName);
            }
            ObjectMetadata meta = new ObjectMetadata();
            String objectName = getObjectName(name);
            LOG.debug("store objectName={}",objectName+" on bucket {}",objectName,bucketName);
            
            client.putObject(bucketName, objectName, stream, meta);

        } catch (Exception e) {
            throw new IOException("Store of " + name + " to s3 failed", e);
        }

    }

    @Override
    public Set<String> listNames() throws IOException {
        if (!client.doesBucketExistV2(bucketName)) {
            return Collections.emptySet();
        }
        Set<String> set = new LinkedHashSet<>();
        ObjectListing data = client.listObjects(bucketName, getObjectPrefix());

        String prefix = getObjectPrefix();
        int prefixLength = prefix.length();

        List<S3ObjectSummary> summaries = data.getObjectSummaries();
        for (S3ObjectSummary summary : summaries) {
            if (summary == null) {
                continue;
            }
            String key = summary.getKey();
            if (key == null || key.length() <= prefixLength) {
                continue;
            }
            String filenameOnly = key.substring(prefixLength);

            set.add(filenameOnly);
        }
        return set;
    }

    private String getObjectName(String name) {
        return getObjectPrefix() + name;
    }

    private String getObjectPrefix() {
        return storagePath + "/" + jobUUID + "/";
    }

    @Override
    public InputStream fetch(String name) throws IOException {
        try {
            String objectName = getObjectName(name);
            LOG.debug("Fetching objectName={} from bucket={}", objectName, bucketName);
            
            return client.getObject(bucketName, objectName).getObjectContent();
        } catch (Exception e) {
            throw new IOException("Was not able to fetch object from s3 bucket:" + name);
        }
    }

    @Override
    public void deleteAll() throws IOException {
        String objectPrefix = getObjectPrefix();
        LOG.info("delete all job storage parts from prefix:{}", objectPrefix);

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
                if (!listing.isTruncated())
                    return;

                listing = client.listNextBatchOfObjects(listing);
            }
        } catch (RuntimeException e) {
            throw new IOException("Cannot delete all parts from prefix:" + objectPrefix, e);
        }
    }

    @Override
    public boolean isExisting(String name) throws IOException {
        try {
            return client.doesObjectExist(bucketName, getObjectName(name));
        } catch (RuntimeException e) {
            throw new IOException("Cannot check existence", e);
        }
    }

}
