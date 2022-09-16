// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

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

import com.amazonaws.AmazonClientException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.mercedesbenz.sechub.storage.core.JobStorage;

public class AwsS3JobStorage implements JobStorage {

    private static final Logger LOG = LoggerFactory.getLogger(AwsS3JobStorage.class);

    private AmazonS3 client;
    private String bucketName;
    private String storagePath;
    private UUID jobUUID;
    private TransferManager transferManager;

    AwsS3TransferManagerFactory transferManagerFactory = new AwsS3TransferManagerFactory();

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
        this.transferManager = transferManagerFactory.create(client);
    }

    @Override
    public void store(String name, InputStream inputStream, long contentLength) throws IOException {
        requireNonNull(name, "name may not be null!");
        requireNonNull(inputStream, "inputStream may not be null!");

        if (contentLength < 0) {
            throw new IllegalArgumentException("Content length cannot be negative!");
        }

        storeInS3(name, inputStream, contentLength);
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

    @Override
    public void store(String name, InputStream inputStream) throws IOException {
        requireNonNull(name, "name may not be null!");
        requireNonNull(inputStream, "inputStream may not be null!");

        storeInS3(name, inputStream, null);
    }

    private void storeInS3(String name, InputStream inputStream, Long contentLength) throws IOException {
        try (InputStream stream = inputStream) {
            if (!client.doesBucketExistV2(bucketName)) {
                client.createBucket(bucketName);
            }
            ObjectMetadata meta = new ObjectMetadata();

            if (contentLength != null) {
                meta.setContentLength(contentLength);
            }

            String objectName = getObjectName(name);
            LOG.debug("Start upload of objectName={}", objectName + " to bucket {}", objectName, bucketName);

            // The Transfer Manager is by default non-blocking, which means it will not wait
            // for the upload to finish and returns immediately
            Upload upload = transferManager.upload(bucketName, objectName, stream, meta);

            ProgressListener progressListener = createProgressListener(upload, name);
            upload.addProgressListener(progressListener);

            try {
                upload.waitForCompletion();
                LOG.debug("Successfully uploaded objectName={}", objectName + " to bucket {}", objectName, bucketName);
            } catch (AmazonClientException e) {
                LOG.error("Error while uploading objectName={}", objectName + " to bucket {}", objectName, bucketName);
                LOG.error(e.getMessage());
            }

        } catch (Exception e) {
            throw new IOException("Store of " + name + " to s3 failed", e);
        }
    }

    private ProgressListener createProgressListener(Upload upload, String objectName) {
        ProgressListener progressListener = new ProgressListener() {
            private int previousPercentTransferred = 0;

            private int getPreviousPercentTransferred() {
                return previousPercentTransferred;
            }

            private void setPreviousPercentTransferred(int previousPercentTransferred) {
                this.previousPercentTransferred = previousPercentTransferred;
            }

            private void resetPreviousPercentTransferred() {
                setPreviousPercentTransferred(0);
            }

            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                if (upload == null) {
                    return;
                }

                ProgressEventType progressEventType = progressEvent.getEventType();

                if (progressEventType.isByteCountEvent()) {
                    int percentTransferred = (int) upload.getProgress().getPercentTransferred();
                    printStatusMessage(percentTransferred);
                }

                switch (progressEvent.getEventType()) {
                case TRANSFER_COMPLETED_EVENT:
                    LOG.info("Transfer of {} completed.", objectName);
                    resetPreviousPercentTransferred();
                    break;
                case TRANSFER_FAILED_EVENT:
                    resetPreviousPercentTransferred();
                    LOG.error("Transfer of {} failed.", objectName);
                    break;
                case TRANSFER_STARTED_EVENT:
                    LOG.info("Transfer of {} started.", objectName);
                    resetPreviousPercentTransferred();
                    break;
                default:
                    break;

                }
            }

            private void printStatusMessage(int percentTransferred) {
                if (percentTransferred > getPreviousPercentTransferred()) {
                    // print progress only every 10 percent
                    if ((percentTransferred % 10) == 0) {
                        setPreviousPercentTransferred(percentTransferred);
                        LOG.debug("Percent transfered: " + percentTransferred);
                    }
                }
            }

        };

        return progressListener;
    }
}
