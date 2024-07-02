// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import static java.util.Objects.*;

import java.util.UUID;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.JobStorageFactory;
import com.mercedesbenz.sechub.storage.core.S3Setup;

public class AwsS3JobStorageFactory implements JobStorageFactory {

    private AmazonS3 s3Client;
    private String bucketName;

    public AwsS3JobStorageFactory(S3Setup s3Setup) {
        requireNonNull(s3Setup, "s3setup may not be null!");
        if (!s3Setup.isAvailable()) {
            throw new IllegalStateException("S3 setup not available!");
        }

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Setup.getAccessKey(), s3Setup.getSecretkey());

        ClientConfiguration clientConfiguration = new ClientConfiguration();

        clientConfiguration.setConnectionTimeout(s3Setup.getConnectionTimeoutInMilliseconds());
        clientConfiguration.setSocketTimeout(s3Setup.getSocketTimeoutInMilliseconds());
        clientConfiguration.setRequestTimeout(s3Setup.getRequestTimeOutInMilliseconds());
        clientConfiguration.setClientExecutionTimeout(s3Setup.getClientExecutionTimeoutInMilliseconds());

        clientConfiguration.setMaxConnections(s3Setup.getMaximumAllowedConnections());
        clientConfiguration.setConnectionTTL(s3Setup.getConnectionTTLinMilliseconds());
        clientConfiguration.setConnectionMaxIdleMillis(s3Setup.getConnectionMaxIdleInMilliseconds());
        clientConfiguration.setValidateAfterInactivityMillis(s3Setup.getValidateAfterInactivityInMilliseconds());

        clientConfiguration.setSignerOverride(s3Setup.getSignerOverride());

        s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Setup.getEndPoint(), Regions.DEFAULT_REGION.name()))
                .withClientConfiguration(clientConfiguration).build();

        bucketName = s3Setup.getBucketName();
    }

    @Override
    public JobStorage createJobStorage(String storagePath, UUID jobUUID) {
        return new AwsS3JobStorage(s3Client, bucketName, storagePath, jobUUID);
    }

}
